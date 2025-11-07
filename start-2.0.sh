#!/bin/bash

echo "=== 修仙挂机游戏 2.0 启动脚本 ==="

# 检查Java环境
if ! command -v java &> /dev/null; then
    echo "❌ 错误: 未找到Java，请安装Java 8或更高版本"
    exit 1
fi

# 检查Java版本
JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 8 ]; then
    echo "❌ 错误: Java版本过低，需要Java 8或更高版本"
    exit 1
fi

echo "✅ Java版本检查通过: $(java -version 2>&1 | head -n 1)"

# 检查JAR文件
JAR_FILE="target/xiuxian-game.jar"
if [ ! -f "$JAR_FILE" ]; then
    echo "❌ 错误: 未找到JAR文件 $JAR_FILE"
    echo "请先运行: mvn clean package"
    exit 1
fi

echo "✅ JAR文件检查通过: $JAR_FILE"

# 检查端口8080是否被占用
if lsof -Pi :8080 -sTCP:LISTEN -t >/dev/null 2>&1; then
    echo "⚠️  警告: 端口8080已被占用，应用可能启动失败"
    echo "请关闭占用端口8080的进程或修改application.properties中的端口配置"
fi

# 启动应用
echo "🚀 正在启动修仙挂机游戏..."
echo "📝 日志将输出到 console.log 文件"
echo "🔗 访问地址: http://localhost:8080/xiuxian-game/"
echo "🔧 H2控制台: http://localhost:8080/h2-console"
echo "按 Ctrl+C 停止应用"
echo "=================================="

java -jar "$JAR_FILE" > console.log 2>&1 &

# 等待应用启动
sleep 3

# 检查应用是否启动成功
if ps -p $! > /dev/null 2>&1; then
    echo "✅ 应用启动成功！PID: $!"
    echo "📋 查看日志: tail -f console.log"
    echo "🛑 停止应用: kill $!"
else
    echo "❌ 应用启动失败，请检查 console.log 日志文件"
    echo "📋 查看错误日志: cat console.log"
    exit 1
fi
