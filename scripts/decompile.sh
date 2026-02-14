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
