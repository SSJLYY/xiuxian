#!/bin/bash

# 测试装备系统修复脚本

echo "========================================"
echo "装备系统修复测试"
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
echo "2. 获取玩家装备列表 (带详细信息)..."
EQUIPMENT_RESPONSE=$(curl -s -X GET "$BASE_URL/api/equipment/details" \
  -H "Authorization: Bearer $TOKEN")

echo "装备列表响应: $EQUIPMENT_RESPONSE"

echo ""
echo "3. 获取已装备装备列表 (带详细信息)..."
EQUIPPED_RESPONSE=$(curl -s -X GET "$BASE_URL/api/equipment/equipped/details" \
  -H "Authorization: Bearer $TOKEN")

echo "已装备装备响应: $EQUIPPED_RESPONSE"

echo ""
echo "4. 测试装备物品..."
# 首先获取一个可装备的物品
AVAILABLE_RESPONSE=$(curl -s -X GET "$BASE_URL/api/equipment/available" \
  -H "Authorization: Bearer $TOKEN")

echo "可装备物品响应: $AVAILABLE_RESPONSE"

# 提取第一个装备的ID
EQUIPMENT_ID=$(echo $AVAILABLE_RESPONSE | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)

if [ -n "$EQUIPMENT_ID" ]; then
  echo "找到可装备物品，ID: $EQUIPMENT_ID"
  
  # 获取玩家装备ID
  PLAYER_EQUIPMENT_RESPONSE=$(curl -s -X GET "$BASE_URL/api/equipment" \
    -H "Authorization: Bearer $TOKEN")
  
  PLAYER_EQUIPMENT_ID=$(echo $PLAYER_EQUIPMENT_RESPONSE | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
  
  if [ -n "$PLAYER_EQUIPMENT_ID" ]; then
    echo "玩家装备ID: $PLAYER_EQUIPMENT_ID"
    
    # 尝试装备物品
    EQUIP_RESPONSE=$(curl -s -X POST "$BASE_URL/api/equipment/equip" \
      -H "Authorization: Bearer $TOKEN" \
      -H "Content-Type: application/x-www-form-urlencoded" \
      -d "playerEquipmentId=$PLAYER_EQUIPMENT_ID&slot=weapon")
    
    echo "装备响应: $EQUIP_RESPONSE"
    
    # 尝试卸下装备
    UNEQUIP_RESPONSE=$(curl -s -X POST "$BASE_URL/api/equipment/unequip" \
      -H "Authorization: Bearer $TOKEN" \
      -H "Content-Type: application/x-www-form-urlencoded" \
      -d "playerEquipmentId=$PLAYER_EQUIPMENT_ID")
    
    echo "卸下响应: $UNEQUIP_RESPONSE"
  else
    echo "❌ 无法获取玩家装备ID"
  fi
else
  echo "❌ 无法找到可装备物品"
fi

echo ""
echo "========================================"
echo "测试完成"
echo "========================================"