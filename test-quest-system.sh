#!/bin/bash

# 测试任务系统功能脚本

echo "========================================"
echo "任务系统功能测试"
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
echo "2. 获取所有任务..."
QUESTS_RESPONSE=$(curl -s -X GET "$BASE_URL/api/quests" \
  -H "Authorization: Bearer $TOKEN")

echo "任务列表响应: $QUESTS_RESPONSE"

echo ""
echo "3. 获取日常任务..."
DAILY_RESPONSE=$(curl -s -X GET "$BASE_URL/api/quests/daily" \
  -H "Authorization: Bearer $TOKEN")

echo "日常任务响应: $DAILY_RESPONSE"

echo ""
echo "4. 获取周常任务..."
WEEKLY_RESPONSE=$(curl -s -X GET "$BASE_URL/api/quests/weekly" \
  -H "Authorization: Bearer $TOKEN")

echo "周常任务响应: $WEEKLY_RESPONSE"

echo ""
echo "5. 获取月常任务..."
MONTHLY_RESPONSE=$(curl -s -X GET "$BASE_URL/api/quests/monthly" \
  -H "Authorization: Bearer $TOKEN")

echo "月常任务响应: $MONTHLY_RESPONSE"

echo ""
echo "6. 刷新日常任务..."
REFRESH_RESPONSE=$(curl -s -X POST "$BASE_URL/api/quests/daily/refresh" \
  -H "Authorization: Bearer $TOKEN")

echo "刷新日常任务响应: $REFRESH_RESPONSE"

echo ""
echo "========================================"
echo "测试完成"
echo "========================================"