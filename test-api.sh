#!/bin/bash

# 修仙游戏 API 测试脚本

# 配置
BASE_URL="http://localhost:8081"
USERNAME="testuser$(date +%s)"
PASSWORD="testpass123"
EMAIL="test${USERNAME}@example.com"
NICKNAME="测试玩家$(date +%s)"

echo "========================================"
echo "修仙游戏 API 测试脚本"
echo "========================================"

# 1. 测试注册接口
echo ""
echo "1. 测试用户注册..."
REGISTER_RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/register" \
  -H "Content-Type: application/json" \
  -d "{
    \"username\": \"$USERNAME\",
    \"password\": \"$PASSWORD\",
    \"email\": \"$EMAIL\",
    \"nickname\": \"$NICKNAME\"
  }")

echo "注册响应: $REGISTER_RESPONSE"

# 2. 测试登录接口
echo ""
echo "2. 测试用户登录..."
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d "{
    \"username\": \"$USERNAME\",
    \"password\": \"$PASSWORD\"
  }")

echo "登录响应: $LOGIN_RESPONSE"

# 提取 JWT Token
TOKEN=$(echo $LOGIN_RESPONSE | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

if [ -z "$TOKEN" ]; then
  echo "❌ 登录失败，无法获取 Token"
  exit 1
fi

echo "✅ 登录成功，获取 Token: ${TOKEN:0:20}..."

# 3. 测试获取玩家信息
echo ""
echo "3. 测试获取玩家信息..."
PROFILE_RESPONSE=$(curl -s -X GET "$BASE_URL/api/player/profile" \
  -H "Authorization: Bearer $TOKEN")

echo "玩家信息响应: $PROFILE_RESPONSE"

# 4. 测试开始修炼
echo ""
echo "4. 测试开始修炼..."
CULTIVATE_RESPONSE=$(curl -s -X POST "$BASE_URL/api/player/cultivate" \
  -H "Authorization: Bearer $TOKEN")

echo "开始修炼响应: $CULTIVATE_RESPONSE"

# 5. 测试停止修炼
echo ""
echo "5. 测试停止修炼..."
STOP_RESPONSE=$(curl -s -X POST "$BASE_URL/api/player/cultivate/stop" \
  -H "Authorization: Bearer $TOKEN")

echo "停止修炼响应: $STOP_RESPONSE"

# 6. 测试技能商店
echo ""
echo "6. 测试技能商店..."
SKILL_SHOP_RESPONSE=$(curl -s -X GET "$BASE_URL/api/shop/skills" \
  -H "Authorization: Bearer $TOKEN")

echo "技能商店响应: $SKILL_SHOP_RESPONSE"

# 7. 测试战斗系统
echo ""
echo "7. 测试战斗系统..."
MONSTER_RESPONSE=$(curl -s -X GET "$BASE_URL/api/combat/generate-monster" \
  -H "Authorization: Bearer $TOKEN")

echo "生成怪物响应: $MONSTER_RESPONSE"

# 提取怪物ID
MONSTER_ID=$(echo $MONSTER_RESPONSE | grep -o '"id":[0-9]*' | cut -d':' -f2)

if [ -n "$MONSTER_ID" ]; then
  echo "生成怪物成功，怪物ID: $MONSTER_ID"
  
  # 开始战斗
  echo "开始战斗..."
  COMBAT_RESPONSE=$(curl -s -X POST "$BASE_URL/api/combat/start/$MONSTER_ID" \
    -H "Authorization: Bearer $TOKEN")
  
  echo "战斗响应: $COMBAT_RESPONSE"
else
  echo "❌ 生成怪物失败"
fi

# 8. 测试获取战斗历史
echo ""
echo "8. 测试获取战斗历史..."
HISTORY_RESPONSE=$(curl -s -X GET "$BASE_URL/api/combat/history?limit=5" \
  -H "Authorization: Bearer $TOKEN")

echo "战斗历史响应: $HISTORY_RESPONSE"

echo ""
echo "========================================"
echo "API 测试完成"
echo "========================================"