#!/bin/bash

# 测试技能升级功能脚本

echo "========================================"
echo "技能升级功能测试"
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
echo "2. 获取可学习技能列表..."
AVAILABLE_SKILLS_RESPONSE=$(curl -s -X GET "$BASE_URL/api/skills/available" \
  -H "Authorization: Bearer $TOKEN")

echo "可学习技能响应: $AVAILABLE_SKILLS_RESPONSE"

# 提取第一个技能的ID
SKILL_ID=$(echo $AVAILABLE_SKILLS_RESPONSE | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)

if [ -n "$SKILL_ID" ]; then
  echo "找到可学习技能，ID: $SKILL_ID"
  
  echo ""
  echo "3. 学习技能..."
  LEARN_RESPONSE=$(curl -s -X POST "$BASE_URL/api/skills/learn/$SKILL_ID" \
    -H "Authorization: Bearer $TOKEN")
  
  echo "学习技能响应: $LEARN_RESPONSE"
  
  # 提取玩家技能ID
  PLAYER_SKILL_ID=$(echo $LEARN_RESPONSE | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
  
  if [ -n "$PLAYER_SKILL_ID" ]; then
    echo "玩家技能ID: $PLAYER_SKILL_ID"
    
    echo ""
    echo "4. 获取玩家技能列表..."
    PLAYER_SKILLS_RESPONSE=$(curl -s -X GET "$BASE_URL/api/skills/player" \
      -H "Authorization: Bearer $TOKEN")
    
    echo "玩家技能响应: $PLAYER_SKILLS_RESPONSE"
    
    echo ""
    echo "5. 测试技能升级（使用灵石）..."
    UPGRADE_RESPONSE=$(curl -s -X POST "$BASE_URL/api/skills/$PLAYER_SKILL_ID/upgrade" \
      -H "Authorization: Bearer $TOKEN")
    
    echo "技能升级响应: $UPGRADE_RESPONSE"
    
    echo ""
    echo "6. 测试技能升级（使用技能点）..."
    UPGRADE_BY_POINTS_RESPONSE=$(curl -s -X POST "$BASE_URL/api/skills/$PLAYER_SKILL_ID/upgrade-by-points" \
      -H "Authorization: Bearer $TOKEN")
    
    echo "技能点升级响应: $UPGRADE_BY_POINTS_RESPONSE"
  else
    echo "❌ 无法获取玩家技能ID"
  fi
else
  echo "❌ 无法找到可学习技能"
fi

echo ""
echo "========================================"
echo "测试完成"
echo "========================================"