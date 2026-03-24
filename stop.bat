@echo off
chcp 65001 >nul
title 修仙世界 - 停止程序

echo ========================================
echo       修仙世界 - 停止程序
echo ========================================
echo.

echo 正在查找并停止修仙世界进程...

:: 查找占用8080或8082端口的进程
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":8082" ^| findstr "LISTENING"') do (
    echo 发现进程 PID: %%a
    taskkill /F /PID %%a >nul 2>&1
    if %errorlevel% equ 0 (
        echo [成功] 进程 %%a 已停止
    ) else (
        echo [失败] 无法停止进程 %%a
    )
)

:: 也尝试通过进程名查找
for /f "tokens=2" %%a in ('wmic process where "name='java.exe'" get processid^,commandline ^| findstr "xiuxian"') do (
    echo 发现java进程 PID: %%a
    taskkill /F /PID %%a >nul 2>&1
)

echo.
echo ========================================
echo   修仙世界已停止
echo ========================================
echo.
pause
