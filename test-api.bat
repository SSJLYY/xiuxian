@echo off
chcp 65001 >nul

REM 修仙游戏 API 测试脚本 (Windows)

REM 配置
set BASE_URL=http://localhost:8081
set TIMESTAMP=%RANDOM%
set USERNAME=testuser%TIMESTAMP%
set PASSWORD=testpass123
set EMAIL=test%USERNAME%@example.com
set NICKNAME=测试玩家%TIMESTAMP%

echo ========================================
echo 修仙游戏 API 测试脚本
echo ========================================

REM 1. 测试注册接口
echo.
echo 1. 测试用户注册...
curl -s -X POST "%BASE_URL%/api/auth/register" ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"%USERNAME%\",\"password\":\"%PASSWORD%\",\"email\":\"%EMAIL%\",\"nickname\":\"%NICKNAME%\"}"
echo.

REM 2. 测试登录接口
echo.
echo 2. 测试用户登录...
curl -s -X POST "%BASE_URL%/api/auth/login" ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"%USERNAME%\",\"password\":\"%PASSWORD%\"}"
echo.

REM 3. 测试获取玩家信息 (需要先获取 Token)
echo.
echo 3. 注意: 请手动获取 Token 后测试其他接口
echo    1. 使用浏览器登录游戏
echo    2. 打开开发者工具 (F12)
echo    3. 在 Console 中输入: localStorage.getItem('authToken')
echo    4. 复制 Token 并替换到测试脚本中

echo.
echo ========================================
echo API 测试脚本准备完成
echo ========================================
pause