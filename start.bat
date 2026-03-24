@echo off
chcp 65001 >nul
title 修仙世界启动器

echo ========================================
echo       修仙世界 - 一键启动
echo ========================================
echo.

:: 检查Java
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [错误] 未检测到Java环境，请先安装JDK
    pause
    exit /b 1
)

:: 检查Maven
set MAVEN_PATH=
where mvn >nul 2>&1
if %errorlevel% equ 0 (
    set MAVEN_PATH=mvn
) else (
    if exist "D:\soft\apache-maven-3.5.3\bin\mvn.cmd" (
        set MAVEN_PATH=D:\soft\apache-maven-3.5.3\bin\mvn.cmd
    )
)

:: 检查JAR文件
if not exist "target\xiuxian-game.jar" (
    echo [提示] JAR文件不存在，开始编译...
    if defined MAVEN_PATH (
        "%MAVEN_PATH%" clean package -DskipTests
        if %errorlevel% neq 0 (
            echo [错误] 编译失败
            pause
            exit /b 1
        )
    ) else (
        echo [错误] 无法编译，Maven未找到
        pause
        exit /b 1
    )
)

:: 检查数据库
echo [1/3] 检查数据库...
mysql -h 127.0.0.1 -u root -p123456 -e "SELECT 1" >nul 2>&1
if %errorlevel% neq 0 (
    echo [错误] 数据库连接失败，请检查MySQL
    pause
    exit /b 1
)
echo [OK] 数据库正常

:: 停止旧进程
echo [2/3] 停止旧进程...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":8082" ^| findstr "LISTENING"') do (
    taskkill /F /PID %%a >nul 2>&1
)
timeout /t 1 /nobreak >nul
echo [OK] 旧进程已停止

:: 启动应用
echo [3/3] 启动应用...
if not exist "logs" mkdir logs

start "修仙世界" java -Xms128m -Xmx256m -XX:+UseG1GC -Dfile.encoding=UTF-8 -Dserver.port=8082 -jar "target\xiuxian-game.jar"

:: 等待启动
echo.
echo 等待应用启动...
for /L %%i in (1,1,10) do (
    curl -s http://localhost:8082/login.html >nul 2>&1
    if %errorlevel% equ 0 goto :started
    timeout /t 1 /nobreak >nul
)

echo [错误] 应用启动失败，请检查日志
pause
exit /b 1

:started
echo.
echo ========================================
echo   修仙世界启动成功！
echo   请访问: http://localhost:8082
echo ========================================
echo.
