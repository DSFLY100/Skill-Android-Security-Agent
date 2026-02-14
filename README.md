# **一、简言**

在移动安全领域，完全依赖正则表达式的扫描容易产生海量误报，而完全依赖 AI 阅读代码又受限于上下文窗口和对复杂数据流的计算能力。

本文提出一种**基于 Skill 编排**的审计架构。该架构将传统的静态分析工具（Soot/FlowDroid）封装为 Agent 的“工具箱”，由 Claude Code 等大模型作为“大脑”进行调度与最终决策。

# **二、核心架构思路**

我们可以设计一个 **“漏斗式”** 的分析流水线：

1. **第一层：java代码和资源文件获取 (基础设施层)**
   - Jadx反编译，获取伪码和AndroidManifest.xml
2. **第二层：基于模式匹配的快速筛选 (特征层)****
   **
   - 先用 **Semgrep** 或 **Regex** 快速扫描反编译后的 Java 源码。
   - 寻找特征：硬编码密钥、弱加密算法（ECB模式）、SQL 拼接字符串、Log 打印敏感信息。
   - 分析AndroidManifest.xml中的暴露组件。
3. **第三层：Soot/FlowDroid 路径验证 (数据流层)**
   - 针对第二层发现的“可疑点”，将其作为 Sink（污点汇聚点），进行定向的污点分析。确认用户输入（Source）是否真的能到达这里。
4. **第四层：AI 代码审计 (语义层)**
   - 提取关键代码片段，喂给 LLM。
   - 让 AI 判断：是否存在过滤逻辑？是否是误报？并生成修复建议。

具体架构图示如下：

![图片](https://mmbiz.qpic.cn/mmbiz_png/unnlWdxxboN70Lrqgjn274LiasQS2bpNdWcoiaQ77K2VRB5xZOrKJiaPUibibiagDZv07SwlZBCsPnBJTkTmW2FmicW1OrXMe6S2Y62heoWYK9VJ14/640?wx_fmt=png&from=appmsg&watermark=1&tp=webp&wxfrom=5&wx_lazy=1#imgIndex=0)

目录结构如下：

```
app-security-automation/
├── CLAUDE.md          <-- 核心：Claude Code 的“大脑”配置
├── SKILL.md           <-- 核心：详细的审计技能文档
├── scripts/           <-- 脚本 (quick_scan.sh, analyze_candidates.py)
├── tools/             <-- 工具 (flowdroid, android.jar)
├── targetapks/        <-- 待测 APK
└── files/             <-- 输出结果
```

# **三、详细工作流设计**

## **第一步：源码还原**

Agent 调用 `scripts/decompile.sh`。

将 APK 转换为 Java 伪代码。Agent 会特别关注 `--show-bad-code` 参数，确保即使反编译不完美也能获取尽可能多的逻辑信息。

## **第二步：快速锚定**

面对海量文件，AI 不可能逐行阅读。我们需要先找到“靶点”。

**Agent 调用 scripts/quick_scan.sh**。基于特征库（如 `rawQuery`, `loadUrl`, `Runtime.exec`）进行快速扫描。产出一份包含文件路径和行号的 **“潜在风险列表”**。

## **第三步：污点分析与路径验证**

这是本架构的核心差异点。 正则只能看到“有点像漏洞”，Soot 才能证明“数据确实流过去了”。Agent 针对第二步发现的可疑点，调用 `scripts/analyze_candidates.py`。利用工具 Soot + FlowDroid 将第二步发现的 Sink 点（如 SQL 执行处）作为目标，追踪 Source 点（如 `getIntent`, `EditText`），计算是否存在一条从 Source 到 Sink 的通路。最后产出结构化的数据流报告。例如：“在 `LoginActivity.java` 中，变量 `username` 从 `Intent` 输入，未经净化直接流入 `rawQuery`。”

## **第四步：AI 智能裁决**

这是传统工具无法替代的环节。工具不懂业务逻辑（比如这个 SQL 拼接是否只是在查本地配置表？），但 AI 懂。

Agent 读取源码 + FlowDroid 报告，结合数据流证据和业务逻辑，判定是误报还是漏洞风险。

# 四、各个脚本的功能详解

## decompile.sh

代码内容如下：

```
#!/bin/bash

# 用法: ./scripts/decompile.sh <path_to_apk> <output_dir>

APK_PATH=$1
OUTPUT_DIR=${2:-"../wsource_dump"} # 默认为 source_dump 目录
# 建议：将 JADX_JAR 路径改为配置变量或相对路径，确保通用性
JADX_JAR="../tools/jadx-1.5.3-all.jar" 

if [ -z "$APK_PATH" ]; then
    echo "Usage: $0 <apk_file> [output_dir]"
    exit 1
fi

if [ ! -f "$JADX_JAR" ]; then
    echo "Error: JADX jar not found at $JADX_JAR"
    exit 1
fi

echo "[*] Starting Decompilation for $APK_PATH..."

# 清理旧目录，防止混淆
if [ -d "$OUTPUT_DIR" ]; then
    echo "[-] Cleaning old output directory..."
    rm -rf "$OUTPUT_DIR"
fi

# 执行 Jadx (针对 AI 优化的参数 + 强制 CLI 模式)
# -cp "$JADX_JAR" jadx.cli.JadxCLI : 强制调用 CLI 主类，避免启动 GUI
java -Xmx4g -cp "$JADX_JAR" jadx.cli.JadxCLI \
    -d "$OUTPUT_DIR" \
    --show-bad-code \
    --deobf \
    --threads-count 4 \
    --no-imports \
    --comments-level none \
    "$APK_PATH"

# 检查返回值
if [ $? -eq 0 ]; then
    echo "[+] Decompilation Successful! Output: $OUTPUT_DIR"
    
    # 验证目录结构，方便后续脚本定位 source_root
    if [ -d "$OUTPUT_DIR/sources" ]; then
        echo "[+] Source Root located at: $OUTPUT_DIR/sources"
    else
        # 某些旧版 jadx 可能会直接输出在根目录，做个兼容提示
        echo "[!] Warning: 'sources' subdirectory not found. Check structure."
    fi
else
    echo "[!] Decompilation With Errors."
    exit 1
fi

```

这个脚本 `decompile.sh` 是自动化安全审计流程中的**第一步**，其核心作用是**将 APK 文件还原为可读的 Java 源代码**。

以下是它的主要功能描述：

**1. 强制命令行模式 (Headless Automation)**

- **功能**: 通过 `-cp "$JADX_JAR" jadx.cli.JadxCLI` 显式调用 Jadx 的命令行接口类。
- **目的**: 防止在服务器、Docker 或没有图形界面的环境中误启动 Jadx 的 GUI 窗口，确保脚本能静默运行。

**2. 环境清理与初始化 (Clean Setup)**

- **功能**: 运行前检查输入参数，并执行 `rm -rf "$OUTPUT_DIR"`。
- **目的**: 确保每次反编译都是“干净”的，防止旧的扫描结果（残留文件）干扰本次分析，避免误报。

**3. 针对“机器阅读”优化的反编译配置**

脚本使用了一组特定的参数，生成的代码是给 **Grep** 或 **AI** 看的，而不是给人看的：

- `--show-bad-code`: **保留反编译失败的代码**。对于安全审计来说，即使代码反编译不完整（比如只有字节码结构），也可能包含关键的字符串或逻辑，不能直接丢弃。
- `--deobf`: **自动反混淆**。尝试给混淆过的变量（如 `a.b.c`）重命名为有意义的名字，便于后续分析。
- `--no-imports`: **移除 import 语句**。对于正则扫描和 AI 分析，`import` 语句通常是噪音，去掉可以减少文件体积和上下文干扰。
- `--comments-level none`: **移除注释**。去掉了 Jadx 生成的自动注释（如“// from class: ...”），让代码更纯净，减少 Token 消耗。

**4. 性能保障**

- `java -Xmx4g`: 分配 **4GB 内存**，防止反编译大型 APK（如 100MB+ 的应用）时发生 OOM（内存溢出）崩溃。
- `--threads-count 4`: 启用 **4 线程**并行处理，加快反编译速度。

**5. 输出结构验证**

- **功能**: 反编译结束后，自动检查 `$OUTPUT_DIR/sources` 目录是否存在。
- **目的**: 为下一步（Python 脚本分析）做检查。如果目录结构不对，提前报错，避免后续脚本因为找不到路径而瞎跑。

## quick_scan.sh

代码内容如下：

```
#!/bin/bash

SOURCE_DIR=$1
OUTPUT_CSV="../files/scan_candidates.csv" # 中间文件
TEMP_DIR=$(mktemp -d)

if [ -z "$SOURCE_DIR" ]; then
    echo "Usage: $0 <source_dir>"
    exit 1
fi

# 初始化 CSV 头
echo "type,filepath,linenum,content" > "$OUTPUT_CSV"

echo "=== Android Security Quick Scan (Parallel Mode) ==="
echo "Target: $SOURCE_DIR"

# 定义辅助函数：提取信息并写入 CSV
# 参数: $1=漏洞类型, $2=输入文件
process_results() {
    local v_type=$1
    local input_file=$2
    
    # 逐行读取 grep 结果 (格式: file:line:content)
    # 使用 awk 处理冒号分隔，注意文件路径可能包含冒号的情况需要小心处理，
    # 这里假设 standard grep output format: path:line:content
    while read -r line; do
        # 提取文件路径 (第一个冒号前)
        filepath=$(echo "$line" | cut -d: -f1)
        # 提取行号 (第二个冒号前)
        linenum=$(echo "$line" | cut -d: -f2)
        # 提取内容 (剩余部分)，移除可能的逗号以防破坏 CSV
        content=$(echo "$line" | cut -d: -f3- | tr -d ',')
        
        echo "$v_type,$filepath,$linenum,$content" >> "$OUTPUT_CSV"
    done < "$input_file"
}

scan_sql() {
    # 增加过滤条件，减少误报
    grep -rnE "rawQuery|execSQL" "$SOURCE_DIR" | grep "+" | head -n 20 > "$TEMP_DIR/sql.raw"
    process_results "SQL_INJECTION" "$TEMP_DIR/sql.raw"
}

scan_secrets() {
    # 排除 BuildConfig 和 R.java
    grep -rnEi "api_key|access_token|secret_key|password =" "$SOURCE_DIR" | grep -vE "BuildConfig.java|R.java" | head -n 20 > "$TEMP_DIR/secrets.raw"
    process_results "HARDCODED_SECRET" "$TEMP_DIR/secrets.raw"
}

scan_webview() {
    grep -rn "setJavaScriptEnabled" "$SOURCE_DIR" | head -n 20 > "$TEMP_DIR/webview.raw"
    process_results "WEBVIEW_RISK" "$TEMP_DIR/webview.raw"
}

scan_cmd_injection() {
    # 扫描 Runtime.exec 或 ProcessBuilder
    grep -rnE "Runtime\.getRuntime\(\)\.exec|ProcessBuilder" "$SOURCE_DIR" | head -n 20 > "$TEMP_DIR/cmd.raw"
    process_results "CMD_INJECTION" "$TEMP_DIR/cmd.raw"
}

scan_path_traversal() {
    # 简单的文件操作扫描 (注意：这可能会有很多误报，需要 FlowDroid 过滤)
    grep -rnE "new File\(|FileInputStream" "$SOURCE_DIR" | grep "+" | head -n 20 > "$TEMP_DIR/file.raw"
    process_results "PATH_TRAVERSAL" "$TEMP_DIR/file.raw"
}

# 并发执行
scan_sql & PID1=$!
scan_secrets & PID2=$!
scan_webview & PID3=$!
scan_cmd_injection & PID5=$!
scan_path_traversal & PID6=$!

wait $PID1 $PID2 $PID3

echo "=== Scan Finished ==="
echo "candidates saved to: $OUTPUT_CSV"
# 同时也打印给人看
cat "$OUTPUT_CSV" | column -t -s,

rm -rf "$TEMP_DIR"

```

这个脚本 是自动化安全审计流程中的**第二步**，接在反编译之后执行。

它的核心作用是**利用正则表达式（Regex）进行快速、粗粒度的“海选”**，从海量的源代码中定位出所有“疑似”漏洞的代码行，并生成结构化的 CSV 清单，供后续的深度分析工具 FlowDroid 使用。

以下是它的主要功能描述：

**1. 并行化正则扫描 (Parallel Grep Scanning)**

- **功能**: 同时启动多个后台进程 (`&`) 分别扫描不同类型的漏洞（SQL注入、硬编码密钥、WebView 风险等）。
- **目的**: 充分利用多核 CPU，极大缩短扫描时间。相比于串行执行 `grep`，这种方式在处理大型项目时效率倍增。

**2. 多维度漏洞模式匹配 (Pattern Matching)**

脚本内置了针对 Android 常见高危漏洞的检测规则：

- **SQL 注入 (`SQL_INJECTION`)**: 查找 `rawQuery` 或 `execSQL`，并尝试通过 `grep "+"` 筛选存在**字符串拼接**的代码行（拼接才是注入的根源）。
- **硬编码密钥 (`HARDCODED_SECRET`)**: 查找 `api_key`, `access_token` 等敏感关键词，同时**智能排除** `BuildConfig.java` 和 `R.java` 等自动生成的干扰文件。
- **命令注入 (`CMD_INJECTION`)**: 监测 `Runtime.exec` 和 `ProcessBuilder`，这是执行系统命令的高危入口。
- **WebView 风险 (`WEBVIEW_RISK`)**: 检查 `setJavaScriptEnabled`，这是开启 XSS 攻击面的常见配置。
- **路径遍历 (`PATH_TRAVERSAL`)**: 寻找 `new File` 或 `FileInputStream` 并伴随字符串拼接的操作。

**3. 数据清洗与结构化输出 (ETL)**

- 功能: 

  定义了 `process_results `函数，将 `grep` 原始的输出（文件:行号:内容）解析并转换为标准的 CSV 格式：

  - `type`: 漏洞类别（如 SQL_INJECTION）
  - `filepath`: 文件路径
  - `linenum`: 代码行号
  - `content`: 代码片段（并移除了可能破坏 CSV 结构的逗号）

- **目的**: 将非结构化的文本日志转换为机器可读的 **CSV 数据集**，作为 Python 分析脚本的标准输入接口。

**4. 噪音控制 (Noise Reduction)**

- **功能**: 在每个 `grep` 管道末尾使用了 `head -n 20`。
- **目的**: 限制每个漏洞类型的最大候选数量。这是一种**启发式策略**：防止某个常见的工具类（如 LogUtil）导致产生成千上万条低质量结果，从而堵塞后续的 FlowDroid 分析队列。

**5. 临时环境管理**

- **功能**: 使用 `mktemp -d` 创建隔离的临时目录存放中间结果，脚本结束时自动 `rm -rf` 清理。
- **目的**: 保证文件系统整洁，避免并发运行时的文件冲突。

## analyze_candidates.py

代码内容如下：

```
#!/usr/bin/env python3
import csv
import sys
import os
import json
import subprocess
import xml.etree.ElementTree as ET

# === 路径自动配置 ===
# 获取当前脚本所在的目录 (例如 .../scripts)
SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
# 获取项目根目录 (例如 .../app-security-automation)
BASE_DIR = os.path.dirname(SCRIPT_DIR)

# === 工具配置 ===
# 使用 os.path.join 确保路径在任何系统下都正确
# 假设你的目录结构是项目根目录下有 tools 文件夹
FLOWDROID_JAR = os.path.join(BASE_DIR, "tools", "soot-infoflow-cmd-jar-with-dependencies.jar")
ANDROID_PLATFORMS = os.path.join(BASE_DIR, "tools", "android.jar") 
SOURCES_SINKS_FILE = os.path.join(BASE_DIR, "tools", "SourcesAndSinks.txt")

# 定义哪些漏洞类型需要启动 FlowDroid (污点分析)
TAINT_ANALYSIS_TARGETS = [
    "SQL_INJECTION",   
    "CMD_INJECTION",   
    "PATH_TRAVERSAL",  
    "WEBVIEW_LOAD_URL" 
]

def get_class_name(filepath, source_root):
    """
    将文件路径转换为 Java 类名
    """
    source_root = os.path.abspath(source_root)
    filepath = os.path.abspath(filepath)
    
    try:
        # 计算相对路径
        rel_path = os.path.relpath(filepath, source_root)
        if rel_path.endswith(".java"):
            rel_path = rel_path[:-5]
        # 替换分隔符为点
        return rel_path.replace(os.sep, ".")
    except ValueError:
        # 兜底：如果文件不在 source_root 下，直接用文件名
        return os.path.basename(filepath).replace(".java", "")

def parse_flowdroid_xml(xml_file):
    """
    解析 FlowDroid 生成的 XML 报告
    """
    results = []
    try:
        tree = ET.parse(xml_file)
        root = tree.getroot()
        for result in root.findall(".//Result"):
            source = result.find("Source")
            sink = result.find("Sink")
            if source is not None and sink is not None:
                results.append({
                    "source": source.get("Statement"),
                    "sink": sink.get("Statement")
                })
    except Exception as e:
        print(f"[!] XML Parse Error: {e}")
        return []
    return results

def run_flowdroid(apk_path, class_name):
    """
    调用 Java 运行 FlowDroid
    """
    # 生成临时的 xml 结果文件名
    output_xml = f"flow_results_{class_name.split('.')[-1]}.xml"
    
    # 构造命令
    cmd = [
        "java", "-jar", FLOWDROID_JAR,
        "-a", apk_path,
        "-p", ANDROID_PLATFORMS,
        "-s", SOURCES_SINKS_FILE,
        "--output", output_xml,
        "--outputformat", "xml",
        "--taintanalysis", "apcontext", # 上下文敏感分析
        "--no-callback-analyzers"       # 禁用回调分析以加速
    ]
    
    print(f"[*] Executing FlowDroid for class: {class_name} ...")
    try:
        # 设置超时时间为 300秒 (5分钟)
        subprocess.run(cmd, timeout=300, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
        
        if os.path.exists(output_xml):
            findings = parse_flowdroid_xml(output_xml)
            os.remove(output_xml) # 清理临时文件
            return findings
        else:
            return None
    except subprocess.TimeoutExpired:
        print(f"[!] FlowDroid timed out for {class_name}")
        return None
    except Exception as e:
        print(f"[!] FlowDroid failed: {e}")
        return None

def main():
    if len(sys.argv) < 4:
        print("Usage: python3 analyze_candidates.py <apk_path> <csv_file> <source_dump_dir>")
        sys.exit(1)

    apk_path = sys.argv[1]
    csv_file = sys.argv[2]
    source_root = sys.argv[3]
    
    # 输出文件路径
    output_json = os.path.join(BASE_DIR, "files", "final_audit_report.json")
    
    # 确保输出目录存在
    os.makedirs(os.path.dirname(output_json), exist_ok=True)

    # 检查工具是否存在
    if not os.path.exists(FLOWDROID_JAR):
        print(f"[Error] FlowDroid jar not found at: {FLOWDROID_JAR}")
        sys.exit(1)
    if not os.path.exists(ANDROID_PLATFORMS):
        print(f"[Error] Android Platforms not found at: {ANDROID_PLATFORMS}")
        sys.exit(1)

    final_report = []

    try:
        # 读取 CSV
        with open(csv_file, 'r', encoding='utf-8', errors='ignore') as f:
            reader = csv.DictReader(f)
            for row in reader:
                vuln_type = row.get('type', 'UNKNOWN')
                filepath = row.get('filepath', '')
                linenum = row.get('linenum', '0')
                content = row.get('content', '')

                report_item = {
                    "type": vuln_type,
                    "file": filepath,
                    "line": linenum,
                    "code_snippet": content,
                    "analysis_mode": "Static Pattern Match"
                }

                # 核心逻辑：判断是否需要跑 FlowDroid
                if vuln_type in TAINT_ANALYSIS_TARGETS:
                    # 只有当成功解析出类名时才跑
                    class_name = get_class_name(filepath, source_root)
                    
                    if class_name and not class_name.endswith(".java"):
                        # 执行分析
                        flow_data = run_flowdroid(apk_path, class_name)
                        
                        # === 这里是之前报错的地方，现在已修复 ===
                        if flow_data and len(flow_data) > 0:
                            report_item["flow_analysis"] = {
                                "reachable": True,
                                "evidence": flow_data
                            }
                            report_item["severity"] = "HIGH (Verified Flow)"
                            report_item["analysis_mode"] = "FlowDroid Verified"
                        else:
                            report_item["flow_analysis"] = {
                                "reachable": False,
                                "note": "FlowDroid found no path"
                            }
                            report_item["severity"] = "MEDIUM (Unverified)"
                    else:
                        report_item["note"] = "Skipped FlowDroid: Cannot determine class name"
                else:
                    report_item["flow_analysis"] = "N/A"
                    report_item["severity"] = "Check Manually"

                final_report.append(report_item)

        # 保存结果
        with open(output_json, 'w') as f:
            json.dump(final_report, f, indent=2)
            
        print(f"\n[+] Analysis Complete! Report saved to: {output_json}")

    except Exception as e:
        print(f"[Error] Script failed: {e}")
        import traceback
        traceback.print_exc()

if __name__ == "__main__":
    main()

```

这个 Python 脚本 `analyze_candidates.py` 是自动化安全审计流程中的**第三步（核心分析层）**，也是整个系统的“大脑”。

它的核心作用是**接收正则扫描的“疑似名单”，通过智能调度策略，有选择性地启动 FlowDroid 进行深度的污点分析（Taint Analysis），最终生成确凿的审计报告**。

以下是它的主要功能描述：

**1. 智能分析路由 (Smart Routing)**

- **功能**: 读取 `scan_candidates.csv` 中的每一条记录，根据漏洞类型（`vuln_type`）决定处理方式。
- 策略:
  - **污点敏感型 (Taint Sensitive)**: 对于 SQL 注入、命令注入、路径遍历等，必须验证数据流是否可控，因此**触发 FlowDroid 分析**。
  - **配置/特征型 (Config/Pattern)**: 对于硬编码密钥、日志泄露等，正则匹配即为证据，**跳过 FlowDroid**，直接标记为待人工复核。
- **目的**: 极大地节省分析时间。FlowDroid 非常耗时，只在刀刃上使用它，避免对整个 APK 进行全量分析。

**2. 自动化 FlowDroid 调度 (Orchestration)**

- **功能**: 封装了 `run_flowdroid` 函数，通过 `subprocess` 调用 Java 命令行启动 Soot-Infoflow (FlowDroid)。
- 参数优化:
  - `--taintanalysis apcontext`: 启用上下文敏感分析，提高准确率。
  - `--no-callback-analyzers`: 禁用回调分析以加速运行（一种性能折中）。
  - `timeout=300`: 设置 5 分钟超时机制，防止某个复杂的类导致分析卡死。
- **动态类名解析**: 通过 `get_class_name` 函数，将文件路径（如 `com/example/MyClass.java`）自动转换为 Java 类名（`com.example.MyClass`），这是 FlowDroid 识别目标的必要参数。

**3. 结果验证与分级 (Verification & Triage)**

- **功能**: 解析 FlowDroid 生成的 XML 报告 (`parse_flowdroid_xml`)。
- 逻辑:
  - **High (Verified Flow)**: 如果 FlowDroid 找到了从 Source（输入）到 Sink（危险函数）的完整路径，脚本将该漏洞标记为**高危且实锤**，并附上证据。
  - **Medium (Unverified)**: 如果正则扫描到了危险代码，但 FlowDroid 没跑通（可能是逻辑太复杂或无路径），标记为**中危**，提示需人工复核。
  - **Check Manually**: 对于不需要跑 FlowDroid 的类型，标记为**需人工检查**。

**4. 报告生成 (Reporting)**

- **功能**: 将所有分析结果汇总是，生成标准化的 JSON 报告 (`final_audit_report.json`)。
- **内容**: 包含漏洞类型、文件位置、源代码片段、以及 FlowDroid 的验证结果（Reachable/Unreachable）。
- **目的**: JSON 格式方便后续对接前端展示、甚至直接喂给 LLM（如 Claude/GPT）让其生成自然语言的修复建议。

**5. 环境自适应 (Portability)**

- **功能**: 使用 `os.path.abspath` 和 `os.path.dirname` 自动计算项目根目录。
- **目的**: 无论脚本在哪里运行，都能准确找到 `tools/android.jar` 和 `SourcesAndSinks.txt`，无需用户手动硬编码路径，增强了脚本的健壮性。

# 五、CLAUDE.md

在项目根目录下创建 `CLAUDE.md`，这告诉 Claude Code 如何调用你的脚本。

```
# Claude Code Project Configuration

## Project Description

这是一个 Android 自动化安全审计系统。它结合了 grep 正则扫描（快速）和 FlowDroid 污点分析（深度验证）。
核心逻辑在 `SKILL.md` 中有详细描述。

## Commands

- **Quick Scan**: `bash scripts/quick_scan.sh targetapks/<apk_name>`
  - 作用: 反编译 APK 并生成初步的 `files/scan_candidates.csv`。
- **Deep Analysis**: `python3 scripts/analyze_candidates.py targetapks/<apk_name> files/scan_candidates.csv source_dump/sources`
  - 作用: 读取 CSV，对高危漏洞（如 SQL注入）运行 FlowDroid，生成 JSON 报告。
- **Full Audit**: 依次运行 Quick Scan 和 Deep Analysis。

## Project Structure

- `scripts/`: 包含 Python 和 Bash 自动化脚本。
- `tools/`: 包含 FlowDroid jar 包 (`soot-infoflow...jar`) 和 `android.jar`。
- `files/`: 存放扫描结果。
- `targetapks/`: 存放待扫描的 .apk 文件。

## Coding Style

- Python: Follow PEP 8. Use `os.path.join` for paths.
- Bash: Use standard bash syntax.

## Analysis Workflow (Important)

当你被要求审计一个 APK 时：

1. 请先阅读 `SKILL.md` 了解判断逻辑。
2. 检查 `scripts/` 和 `tools/` 是否存在。
3. 按照 Commands 中的顺序执行工具。
4. 如果脚本执行失败，请检查路径是否正确，并尝试修复路径问题。
5. 最后根据 `files/final_audit_report.json` 的内容生成审计报告
```

# 六、SKILL.md

````
# 基本信息

- name: app-security-scan-automation
- description: 一个专注于 Android 静态应用安全测试 (SAST) 的智能体。通过编排静态分析工具（Semgrep/Soot）与 LLM 语义分析能力，自动检测代码漏洞，并利用逻辑推理大幅降低误报率。

# 能力与工具链

你拥有对以下本地工具链的调用权限和操作知识：

## 快速扫描工具 (`quick_scan.sh`)

- **功能**: 自动反编译 APK 并使用 `grep` 进行正则匹配。
- **输入**: APK 文件路径。
- 输出:
  - `source_dump/`: Java 源码目录。
  - `files/scan_candidates.csv`: 包含疑似漏洞的文件名、行号和代码片段的清单。
- **适用场景**: 初始扫描，发现 SQL 注入、硬编码密钥、Webview 配置、命令执行等所有疑似点。

## 深度分析引擎 (`analyze_candidates.py`)

- **功能**: 读取 CSV 候选列表，根据漏洞类型决定是否启动 FlowDroid 污点分析。
- 核心逻辑:
  - **污点分析 (Taint Analysis)**: 针对 `SQL_INJECTION`, `CMD_INJECTION`, `PATH_TRAVERSAL`, `WEBVIEW_LOAD_URL`。如果 FlowDroid 发现从 Source 到 Sink 的路径，标记为 `HIGH (Verified Flow)`。
  - **静态确认 (Static Check)**: 针对 `HARDCODED_SECRET`, `LOGGING`, `WEAK_CRYPTO`。直接标记为人工复核。
- **依赖**: `tools/android.jar` (便携式 SDK), `tools/soot-infoflow-cmd-jar-with-dependencies.jar`.
- **输出**: `files/final_audit_report.json`。

# 操作标准作业程序 (SOP)

## 1、阶段一：反编译与初步扫描

1. 接收用户提供的 APK 路径（例如 `../targetapks/test.apk`）。

2. 执行 Shell 命令：

   ```bash
   cd scripts
   ./quick_scan.sh <apk_path>
   ```

3. 检查 `files/scan_candidates.csv` 是否生成。如果不为空，进入下一阶段。

## 2、阶段二：智能路由与深度验证

1. 调用 Python 脚本进行验证。**注意**: 必须正确指定源码目录（通常是 `source_dump/sources` 以便正确解析包名）。

2. 执行命令：

   ```bash
   python3 analyze_candidates.py <apk_path> ../files/scan_candidates.csv ../source_dump/sources
   ```

3. 等待脚本运行完成（FlowDroid 可能需要数分钟）。

## 3、阶段三：报告生成与解读

1. 读取 `files/final_audit_report.json`。

2. 按照严重程度排序

   输出报告：

   - 🔴 **HIGH (Verified Flow)**: 必须优先展示。这是 FlowDroid 实锤的漏洞，存在完整的利用链。请展示 `Source` (输入点) 和 `Sink` (触发点)。
   - 🟠 **MEDIUM (Unverified)**: 可能是漏洞，但 FlowDroid 未找到路径（可能是逻辑复杂或误报）。建议人工审计。
   - 🟡 **Low/Info**: 硬编码密钥、日志泄露等配置问题。

## 4、决策逻辑 (Decision Logic)

Agent 在分析过程中必须遵守以下决策树，以节省计算资源：

| 漏洞类型 (Vuln Type)                        | 处理方式             | 理由                                |
| :------------------------------------------ | :------------------- | :---------------------------------- |
| **SQL Injection** (`rawQuery`, `execSQL`)   | ✅ **Run FlowDroid**  | 只有当参数来自用户输入时才危险。    |
| **Cmd Injection** (`Runtime.exec`)          | ✅ **Run FlowDroid**  | 极高风险，必须确认参数是否可控。    |
| **Path Traversal** (`new File`)             | ✅ **Run FlowDroid**  | 过滤掉正常的内部文件操作。          |
| **Hardcoded Secrets** (`"api_key"`)         | 🚫 **Skip FlowDroid** | 字符串存在即漏洞，无需分析流向。    |
| **WebView Config** (`setJavaScriptEnabled`) | 🚫 **Skip FlowDroid** | 这是状态配置，不是数据流问题。      |
| **Logging** (`Log.d`)                       | 🚫 **Skip FlowDroid** | 数量巨大，跑 FlowDroid 会导致超时。 |

## 5、错误处理 (Error Handling)

- **Jadx 失败**: 如果反编译目录为空，提示用户 APK 可能损坏或加固。
- **FlowDroid 超时**: 脚本设定了 300秒 超时。如果日志显示 `FlowDroid timed out`，在报告中标记为 "Complexity High - Manual Review Required"。
- **找不到类名**: 如果 `get_class_name` 失败，脚本会跳过 FlowDroid。在报告中注明 "Skipped dynamic analysis due to obfuscation"。

## 6、报告生成

- 汇总为Markdown格式报告，报告名称为 安全审计报告.md，报告参考格式如下：

```
## 🔒 安全审计报告

### 🎯 扫描摘要
*   **目标 APK**: `demo.apk`
*   **发现风险点**: 15 个
*   **高危实锤**: 2 个 (经 FlowDroid 验证)

### 🚨 高危漏洞 (High Severity)
**1. SQL 注入**
*   **位置**: `com.example.db.DatabaseHelper.java` : Line 45
*   **证据**: 
    *   输入点 (Source): `etUsername.getText().toString()`
    *   触发点 (Sink): `db.rawQuery(query, null)`
*   **分析**: 这是一个经 FlowDroid 验证的完整攻击路径。用户输入直接拼接到了 SQL 语句中。

### ⚠️ 潜在风险 (Medium/Check Manually)
**1. 硬编码密钥**
*   **位置**: `com.example.Config.java` : Line 12
*   **代码**: `String API_KEY = "123456-secret";`
*   **建议**: 请移除代码中的敏感字符串。

...
```

````

# 七、本地环境试运行

运行Claude Code，输入提示词，开始app安全审计

我这里随便从应用市场下载了一个30MB的apk

<img width="971" height="433" alt="本地打开cc" src="https://github.com/user-attachments/assets/43e84e7b-ea5a-41a8-a227-60319d973fb1" />

执行完成后：

<img width="1039" height="829" alt="cc结果" src="https://github.com/user-attachments/assets/9a7a1b47-4367-42be-beed-5105fecf4aba" />

生成报告展示：

<img width="1325" height="902" alt="报告展示" src="https://github.com/user-attachments/assets/9ad009d3-d675-4ff3-b73d-93da662a73f7" />

# 八、待更新

1、AndroidManifest.xml暴露组件结合反编译代码确认权限分析流程

2、新增漏洞规则

3、扫描逻辑优化，进一步去除误报

# **九、总结**

这只是一个MVP版本，我后续会持续更新，欢迎大家跟我一起讨论。

# 十、广子

欢迎大家关注我的个人微信公众号

![qrcode_for_gh_1cbc1ee9a93e_258](https://github.com/user-attachments/assets/17b81bfd-485a-4efd-a210-9cc075b23af4)
