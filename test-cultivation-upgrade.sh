#!/bin/bash

# 测试修炼升级功能脚本

echo "========================================"
echo "修炼升级功能测试"
echo "========================================"

# 配置
BASE_URL="http://localhost:8081"
USERNAME="testuser$(date +%s)"
PASSWORD="testpass123"
EMAIL="test${USERNAME}@example.com"
NICKNAME="测试玩家$(date +%s)"

echo ""
echo "1. 注册测试用户..."
REGISTER_RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/register" \
  -H "Content-Type: application/json" \
  -d "{
    \"username\": \"$USERNAME\",
    \"password\": \"$PASSWORD\",
    \"email\": \"$EMAIL\",
    \"nickname\": \"$NICKNAME\"
  }")

echo "注册响应: $REGISTER_RESPONSE"

# 提取 JWT Token
TOKEN=$(echo $REGISTER_RESPONSE | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

if [ -z "$TOKEN" ]; then
  echo "❌ 注册失败，无法获取 Token"
  exit 1
fi

echo "✅ 注册成功，获取 Token: ${TOKEN:0:20}..."

echo ""
echo "2. 获取玩家信息..."
PROFILE_RESPONSE=$(curl -s -X GET "$BASE_URL/api/player/profile" \
  -H "Authorization: Bearer $TOKEN")

echo "玩家信息响应: $PROFILE_RESPONSE"

echo ""
echo "3. 开始修炼..."
START_RESPONSE=$(curl -s -X POST "$BASE_URL/api/player/cultivate" \
  -H "Authorization: Bearer $TOKEN")

echo "开始修炼响应: $START_RESPONSE"

echo ""
echo "4. 等待5秒..."
sleep 5

echo ""
echo "5. 停止修炼..."
STOP_RESPONSE=$(curl -s -X POST "$BASE_URL/api/player/cultivate/stop" \
  -H "Authorization: Bearer $TOKEN")

echo "停止修炼响应: $STOP_RESPONSE"

echo ""
echo "6. 再次获取玩家信息（查看经验值和等级变化）..."
PROFILE_RESPONSE_AFTER=$(curl -s -X GET "$BASE_URL/api/player/profile" \
  -H "Authorization: Bearer $TOKEN")

echo "玩家信息响应（修炼后）: $PROFILE_RESPONSE_AFTER"

echo ""
echo "========================================"
echo "测试完成"
echo "========================================"