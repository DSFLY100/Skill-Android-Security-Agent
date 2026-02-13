# Skill-Android-Security-Agent
构建基于 Skill 的 Android 智能审计 Agent

# 一、简言

在移动安全领域，完全依赖正则表达式的扫描容易产生海量误报，而完全依赖 AI 阅读代码又受限于上下文窗口和对复杂数据流的计算能力。
本文提出一种基于 Skill 编排的审计架构。该架构将传统的静态分析工具（Soot/FlowDroid）封装为 Agent 的“工具箱”，由 Claude Code 等大模型作为“大脑”进行调度与最终决策。

# 二、核心架构思路

我们可以设计一个 “漏斗式” 的分析流水线：

- 第一层：java代码和资源文件获取 (基础设施层)

Jadx反编译，获取伪码和AndroidManifest.xml

- 第二层：基于模式匹配的快速筛选 (特征层)

先用 Semgrep 或 Regex 快速扫描反编译后的 Java 源码。
寻找特征：硬编码密钥、弱加密算法（ECB模式）、SQL 拼接字符串、Log 打印敏感信息。
分析AndroidManifest.xml中的暴露组件。

- 第三层：Soot/FlowDroid 路径验证 (数据流层)

针对第二层发现的“可疑点”，将其作为 Sink（污点汇聚点），进行定向的污点分析。确认用户输入（Source）是否真的能到达这里。

- 第四层：AI 代码审计 (语义层)

提取关键代码片段，喂给 LLM。
让 AI 判断：是否存在过滤逻辑？是否是误报？并生成修复建议。

具体架构图示如下：
<img width="1632" height="2898" alt="mermaid-diagram (2)" src="https://github.com/user-attachments/assets/fb6ca6df-c8c9-4e2c-b444-c2e71632d512" />

目录结构如下：

```
app-security-automation/
├── apks/              # 待分析的 APK
├── skill.md                 
├── tools/
│   └── jadx-all.jar        # 反编译工具
└── scripts/                
    ├── decompile.sh        # 封装复杂的反编译命令
    └── quick_scan.sh       # 封装 grep/rg 的扫描逻辑

```

# 三、详细工作流设计

- 第一步：源码还原

Agent 调用 scripts/decompile.sh。
将 APK 转换为 Java 伪代码。Agent 会特别关注 --show-bad-code 参数，确保即使反编译不完美也能获取尽可能多的逻辑信息。

- 第二步：快速锚定

面对海量文件，AI 不可能逐行阅读。我们需要先找到“靶点”。
Agent 调用 scripts/quick_scan.sh。基于特征库（如 rawQuery, loadUrl, Runtime.exec）进行快速扫描。产出一份包含文件路径和行号的 “潜在风险列表”。

- 第三步：污点分析与路径验证

这是本架构的核心差异点。 正则只能看到“有点像漏洞”，Soot 才能证明“数据确实流过去了”。Agent 针对第二步发现的可疑点，调用 scripts/analyze_flow.py。利用工具 Soot + FlowDroid 将第二步发现的 Sink 点（如 SQL 执行处）作为目标，追踪 Source 点（如 getIntent, EditText），计算是否存在一条从 Source 到 Sink 的通路。最后产出结构化的数据流报告。例如：“在 LoginActivity.java 中，变量 username 从 Intent 输入，未经净化直接流入 rawQuery。”

- 第四步：AI 智能裁决

这是传统工具无法替代的环节。工具不懂业务逻辑（比如这个 SQL 拼接是否只是在查本地配置表？），但 AI 懂。
Agent 读取源码 + FlowDroid 报告，结合数据流证据和业务逻辑，判定是误报还是漏洞风险。

# 四、总结

目前把思路分享给大家，我还在调教当中，后续效果还不错的话，我再把我的skill.md、脚本都分享出来。
