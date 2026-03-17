@echo off
chcp 65001 >nul
echo ========================================
echo 🎮 xiuxian挂机游戏启动脚本
echo ========================================
echo.

:: ==================== 配置区域 ====================
:: JVM参数配置
set JAVA_OPTS=-Xms128m -Xmx256m -XX:+UseG1GC -Dfile.encoding=UTF-8
:: 应用端口配置
set APP_PORT=8082
:: 日志目录
set LOG_DIR=.\logs
:: 应用名称
set APP_NAME=xiuxian-game
:: JAR文件路径
set JAR_FILE=.\target\xiuxian-game.jar
:: ==================== 配置区域结束 ====================

:: 创建日志目录
if not exist "%LOG_DIR%" mkdir "%LOG_DIR%"

:: 检查Java环境
echo 📋 检查Java环境...
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ 未检测到Java环境，请先安装Java 8或更高版本
    pause
    exit /b 1
)

:: 获取Java版本
for /f "tokens=3" %%v in ('java -version 2^>^&1 ^| findstr "version"') do (
    set JAVA_VERSION=%%v
)
echo ✅ 检测到Java版本: %JAVA_VERSION:"=%

:: 根据参数执行相应操作
if "%1"=="start" goto start
if "%1"=="stop" goto stop
if "%1"=="restart" goto restart
if "%1"=="status" goto status
if "%1"=="build" goto build
if "%1"=="help" goto help

:: 默认显示帮助信息
goto help

:start
call :check_port
call :start_app
goto :eof

:stop
call :stop_app
goto :eof

:restart
call :stop_app
timeout /t 3 /nobreak >nul
call :start_app
goto :eof

:status
call :status_app
goto :eof

:build
call :build_project
goto :eof

:help
echo ==================== xiuxian挂机游戏启动脚本 ====================
echo 使用方法: start.bat [选项]
echo.
echo 选项:
echo   start     启动应用
echo   stop      停止应用
echo   restart   重启应用
echo   status    查看应用状态
echo   build     编译项目
echo   help      显示此帮助信息
echo.
echo 配置信息:
echo   应用名称: %APP_NAME%
echo   应用端口: %APP_PORT%
echo   JVM参数: %JAVA_OPTS%
echo   日志目录: %LOG_DIR%
echo   JAR文件: %JAR_FILE%
echo.
echo 注意事项:
echo   1. 首次运行请先执行编译: start.bat build
echo   2. 启动前请确保端口 %APP_PORT% 未被占用
echo   3. 应用日志位于: %LOG_DIR%\%APP_NAME%.log
echo   4. 启动后访问: http://localhost:%APP_PORT%/login.html
echo ============================================================
goto :eof

:: 编译项目
:build_project
echo 🏗️ 开始编译项目...

:: 检查Maven环境
mvn -v >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ 未检测到Maven，请先安装Maven
    pause
    exit /b 1
)

:: 执行编译
mvn clean package -DskipTests
if %errorlevel% equ 0 (
    echo ✅ 项目编译成功
) else (
    echo ❌ 项目编译失败
    pause
    exit /b 1
)
goto :eof

:: 检查端口占用
:check_port
echo 🔄 检查端口占用情况...
netstat -ano | findstr ":%APP_PORT%" >nul
if %errorlevel% equ 0 (
    echo ❌ 端口 %APP_PORT% 已被占用
    echo    请先停止占用该端口的程序或修改端口配置
    pause
    exit /b 1
)
goto :eof

:: 启动应用
:start_app
echo 🔧 正在启动应用...

:: 启动应用
start "Xiuxian Game" /min java %JAVA_OPTS% ^
    -Dserver.port=%APP_PORT% ^
    -Dlogging.file.name="%LOG_DIR%\%APP_NAME%.log" ^
    -jar "%JAR_FILE%"

echo ✅ 应用启动命令已执行
echo 📊 查看应用日志: type "%LOG_DIR%\%APP_NAME%.log"
echo 💡 启动完成后，请访问: http://localhost:%APP_PORT%/login.html
echo ⏳ 建议等待10-15秒让应用完全启动后再访问
goto :eof

:: 停止应用
:stop_app
echo 🛑 正在停止应用...
:: 查找并终止Java进程
for /f "tokens=1" %%i in ('jps -v ^| findstr "%JAR_FILE%"') do (
    echo 终止进程 %%i
    taskkill /PID %%i /F >nul 2>&1
)

:: 如果找不到jps命令，则通过端口查找
if %errorlevel% neq 0 (
    for /f "tokens=5" %%i in ('netstat -ano ^| findstr ":%APP_PORT%"') do (
        echo 终止进程 %%i
        taskkill /PID %%i /F >nul 2>&1
    )
)

echo ✅ 应用停止命令已执行
goto :eof

:: 查看应用状态
:status_app
echo 📈 应用状态检查...
:: 查找Java进程
for /f "tokens=1" %%i in ('jps -v ^| findstr "%JAR_FILE%"') do (
    echo ✅ 应用正在运行 (进程ID: %%i)
    echo    端口信息:
    netstat -ano | findstr ":%APP_PORT%"
    goto :eof
)

:: 如果找不到jps命令，则通过端口查找
echo ⚠️ 未检测到应用运行
echo    请检查应用是否已启动
goto :eof