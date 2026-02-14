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
