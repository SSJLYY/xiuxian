@echo off
chcp 65001 >nul
echo ========================================
echo 📝 修仙挂机游戏日志查看工具
echo ========================================
echo.

:: 检查日志目录是否存在
if not exist "logs" (
    echo ❌ 日志目录不存在，请先启动应用
    pause
    exit /b 1
)

echo 📋 可用的日志文件:
echo.
echo 1. 应用日志 (xiuxian-game.log)
echo 2. 错误日志 (xiuxian-game-error.log)
echo 3. SQL日志 (xiuxian-game-sql.log)
echo 4. 性能日志 (xiuxian-game-performance.log)
echo 5. 查看所有日志文件
echo 6. 查看历史日志文件
echo 7. 搜索日志内容
echo 0. 退出
echo.

set /p choice="请选择要查看的日志 (0-7): "

if "%choice%"=="1" (
    echo.
    echo 📖 查看应用日志 (按 Ctrl+C 退出):
    echo ========================================
    if exist "logs\xiuxian-game.log" (
        type "logs\xiuxian-game.log"
    ) else (
        echo 📄 应用日志文件不存在
    )
) else if "%choice%"=="2" (
    echo.
    echo 📖 查看错误日志 (按 Ctrl+C 退出):
    echo ========================================
    if exist "logs\xiuxian-game-error.log" (
        type "logs\xiuxian-game-error.log"
    ) else (
        echo 📄 错误日志文件不存在或无错误记录
    )
) else if "%choice%"=="3" (
    echo.
    echo 📖 查看SQL日志 (按 Ctrl+C 退出):
    echo ========================================
    if exist "logs\xiuxian-game-sql.log" (
        type "logs\xiuxian-game-sql.log"
    ) else (
        echo 📄 SQL日志文件不存在
    )
) else if "%choice%"=="4" (
    echo.
    echo 📖 查看性能日志 (按 Ctrl+C 退出):
    echo ========================================
    if exist "logs\xiuxian-game-performance.log" (
        type "logs\xiuxian-game-performance.log"
    ) else (
        echo 📄 性能日志文件不存在
    )
) else if "%choice%"=="5" (
    echo.
    echo 📂 所有日志文件:
    echo ========================================
    dir /b logs\*.log 2>nul
    if %errorlevel% neq 0 (
        echo 📄 没有找到日志文件
    )
) else if "%choice%"=="6" (
    echo.
    echo 📂 历史日志文件:
    echo ========================================
    dir /b logs\*.gz 2>nul
    if %errorlevel% neq 0 (
        echo 📄 没有找到历史日志文件
    ) else (
        echo.
        echo 💡 查看历史日志请使用: 7-Zip 或其他解压工具打开 .gz 文件
    )
) else if "%choice%"=="7" (
    echo.
    set /p keyword="🔍 请输入要搜索的关键词: "
    if not "!keyword!"=="" (
        echo.
        echo 🔍 搜索结果:
        echo ========================================
        findstr /i "!keyword!" logs\*.log 2>nul
        if %errorlevel% neq 0 (
            echo 📄 没有找到包含 "!keyword!" 的日志记录
        )
    )
) else if "%choice%"=="0" (
    echo 👋 退出日志查看工具
    exit /b 0
) else (
    echo ❌ 无效的选择，请重新运行脚本
)

echo.
echo ========================================
echo 💡 提示:
echo   - 实时查看日志: PowerShell Get-Content logs\xiuxian-game.log -Wait
echo   - 搜索错误: findstr "ERROR" logs\xiuxian-game.log
echo   - 查看最后100行: PowerShell Get-Content logs\xiuxian-game.log -Tail 100
echo ========================================
pause