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
