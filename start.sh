#!/bin/bash

# 修仙挂机游戏启动脚本 (Linux/macOS)

echo "========================================"
echo "🎮 修仙挂机游戏启动脚本"
echo "========================================"
echo

# 检查Java环境
echo "📋 检查Java环境..."
if ! command -v java &> /dev/null; then
    echo "❌ 错误: 未找到Java环境，请先安装Java 1.8或更高版本"
    echo "💡 安装命令: sudo apt-get install openjdk-8-jdk (Ubuntu/Debian)"
    echo "💡 安装命令: brew install openjdk@8 (macOS)"
    exit 1
fi

# 检查Maven环境
echo "📋 检查Maven环境..."
if ! command -v mvn &> /dev/null; then
    echo "❌ 错误: 未找到Maven环境，请先安装Maven"
    echo "💡 安装命令: sudo apt-get install maven (Ubuntu/Debian)"
    echo "💡 安装命令: brew install maven (macOS)"
    exit 1
fi

# 检查MySQL连接
echo "📋 检查MySQL连接..."
echo "💡 请确保MySQL服务已启动，数据库已创建"

# 显示配置信息
echo
echo "📊 当前配置:"
echo "  数据库地址: 47.103.87.55:3306"
echo "  数据库名称: xiuxian_game"
echo "  应用端口: 8081"
echo "  日志目录: logs/"
echo

# 询问是否继续
read -p "🤔 是否继续启动应用? (y/n): " continue
if [[ $continue != "y" && $continue != "Y" ]]; then
    echo "👋 启动已取消"
    exit 0
fi

# 创建日志目录
if [ ! -d "logs" ]; then
    echo "📁 创建日志目录..."
    mkdir -p logs
fi

echo
echo "📝 日志配置信息:"
echo "  日志目录: logs/"
echo "  切分策略: 每天0点自动切分"
echo "  保留策略: 应用日志30天，SQL/性能日志7天"
echo "  压缩格式: GZIP (.gz)"
echo "  查看日志: tail -f logs/xiuxian-game.log"

# 清理并编译项目
echo
echo "🔨 编译项目..."
mvn clean compile -q
if [ $? -ne 0 ]; then
    echo "❌ 编译失败，请检查代码错误"
    exit 1
fi

# 启动应用
echo
echo "🚀 启动应用..."
echo "💡 启动完成后，请访问: http://localhost:8081/login.html"
echo "💡 按 Ctrl+C 可以停止应用"
echo

mvn spring-boot:run

echo
echo "👋 应用已停止"