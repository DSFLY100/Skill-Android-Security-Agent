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