#!/bin/bash

# 修仙挂机游戏日志查看工具 (Linux/macOS)

echo "========================================"
echo "📝 修仙挂机游戏日志查看工具"
echo "========================================"
echo

# 检查日志目录是否存在
if [ ! -d "logs" ]; then
    echo "❌ 日志目录不存在，请先启动应用"
    exit 1
fi

echo "📋 可用的日志文件:"
echo
echo "1. 应用日志 (xiuxian-game.log)"
echo "2. 错误日志 (xiuxian-game-error.log)"
echo "3. SQL日志 (xiuxian-game-sql.log)"
echo "4. 性能日志 (xiuxian-game-performance.log)"
echo "5. 查看所有日志文件"
echo "6. 查看历史日志文件"
echo "7. 搜索日志内容"
echo "8. 实时监控日志"
echo "0. 退出"
echo

read -p "请选择要查看的日志 (0-8): " choice

case $choice in
    1)
        echo
        echo "📖 查看应用日志 (按 q 退出):"
        echo "========================================"
        if [ -f "logs/xiuxian-game.log" ]; then
            less logs/xiuxian-game.log
        else
            echo "📄 应用日志文件不存在"
        fi
        ;;
    2)
        echo
        echo "📖 查看错误日志 (按 q 退出):"
        echo "========================================"
        if [ -f "logs/xiuxian-game-error.log" ]; then
            less logs/xiuxian-game-error.log
        else
            echo "📄 错误日志文件不存在或无错误记录"
        fi
        ;;
    3)
        echo
        echo "📖 查看SQL日志 (按 q 退出):"
        echo "========================================"
        if [ -f "logs/xiuxian-game-sql.log" ]; then
            less logs/xiuxian-game-sql.log
        else
            echo "📄 SQL日志文件不存在"
        fi
        ;;
    4)
        echo
        echo "📖 查看性能日志 (按 q 退出):"
        echo "========================================"
        if [ -f "logs/xiuxian-game-performance.log" ]; then
            less logs/xiuxian-game-performance.log
        else
            echo "📄 性能日志文件不存在"
        fi
        ;;
    5)
        echo
        echo "📂 所有日志文件:"
        echo "========================================"
        ls -la logs/*.log 2>/dev/null || echo "📄 没有找到日志文件"
        ;;
    6)
        echo
        echo "📂 历史日志文件:"
        echo "========================================"
        ls -la logs/*.gz 2>/dev/null || echo "📄 没有找到历史日志文件"
        if ls logs/*.gz >/dev/null 2>&1; then
            echo
            echo "💡 查看历史日志: zcat logs/xiuxian-game-YYYY-MM-DD.log.gz | less"
        fi
        ;;
    7)
        echo
        read -p "🔍 请输入要搜索的关键词: " keyword
        if [ -n "$keyword" ]; then
            echo
            echo "🔍 搜索结果:"
            echo "========================================"
            grep -i "$keyword" logs/*.log 2>/dev/null || echo "📄 没有找到包含 \"$keyword\" 的日志记录"
        fi
        ;;
    8)
        echo
        echo "📖 实时监控应用日志 (按 Ctrl+C 退出):"
        echo "========================================"
        if [ -f "logs/xiuxian-game.log" ]; then
            tail -f logs/xiuxian-game.log
        else
            echo "📄 应用日志文件不存在"
        fi
        ;;
    0)
        echo "👋 退出日志查看工具"
        exit 0
        ;;
    *)
        echo "❌ 无效的选择，请重新运行脚本"
        ;;
esac

echo
echo "========================================"
echo "💡 常用日志命令:"
echo "  实时查看: tail -f logs/xiuxian-game.log"
echo "  搜索错误: grep 'ERROR' logs/xiuxian-game.log"
echo "  查看最后100行: tail -100 logs/xiuxian-game.log"
echo "  查看历史日志: zcat logs/xiuxian-game-2024-11-28.log.gz | less"
echo "  统计错误数: grep -c 'ERROR' logs/xiuxian-game.log"
echo "========================================"