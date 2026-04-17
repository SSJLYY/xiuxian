#!/bin/bash
# 性能基准测试脚本
# 使用 JMeter 进行压力测试

echo "======================================"
echo "修仙挂机游戏 - 性能基准测试"
echo "======================================"

# 测试配置
TARGET_URL="${TARGET_URL:-http://localhost:8081}"
THREADS="${THREADS:-100}"
DURATION="${DURATION:-60}"
OUTPUT_DIR="./performance-test-results"

# 创建输出目录
mkdir -p $OUTPUT_DIR

echo ""
echo "测试配置:"
echo "  目标 URL: $TARGET_URL"
echo "  并发线程数：$THREADS"
echo "  持续时间：${DURATION}s"
echo "  输出目录：$OUTPUT_DIR"
echo ""

# 登录接口压测
echo "[1/5] 测试登录接口..."
jmeter -n -t scripts/jmeter/login-test.jmx \
  -Jtarget_url=$TARGET_URL \
  -Jthreads=$THREADS \
  -Jduration=$DURATION \
  -l $OUTPUT_DIR/login-results.jtl \
  -j $OUTPUT_DIR/login.log \
  -e -o $OUTPUT_DIR/login-report

# 玩家档案查询压测
echo "[2/5] 测试玩家档案查询..."
jmeter -n -t scripts/jmeter/profile-test.jmx \
  -Jtarget_url=$TARGET_URL \
  -Jthreads=$THREADS \
  -Jduration=$DURATION \
  -l $OUTPUT_DIR/profile-results.jtl \
  -j $OUTPUT_DIR/profile.log \
  -e -o $OUTPUT_DIR/profile-report

# 修炼接口压测
echo "[3/5] 测试修炼接口..."
jmeter -n -t scripts/jmeter/cultivate-test.jmx \
  -Jtarget_url=$TARGET_URL \
  -Jthreads=$THREADS \
  -Jduration=$DURATION \
  -l $OUTPUT_DIR/cultivate-results.jtl \
  -j $OUTPUT_DIR/cultivate.log \
  -e -o $OUTPUT_DIR/cultivate-report

# 战斗接口压测
echo "[4/5] 测试战斗接口..."
jmeter -n -t scripts/jmeter/combat-test.jmx \
  -Jtarget_url=$TARGET_URL \
  -Jthreads=$THREADS \
  -Jduration=$DURATION \
  -l $OUTPUT_DIR/combat-results.jtl \
  -j $OUTPUT_DIR/combat.log \
  -e -o $OUTPUT_DIR/combat-report

# 排行榜查询压测
echo "[5/5] 测试排行榜查询..."
jmeter -n -t scripts/jmeter/ranking-test.jmx \
  -Jtarget_url=$TARGET_URL \
  -Jthreads=$THREADS \
  -Jduration=$DURATION \
  -l $OUTPUT_DIR/ranking-results.jtl \
  -j $OUTPUT_DIR/ranking.log \
  -e -o $OUTPUT_DIR/ranking-report

echo ""
echo "======================================"
echo "性能测试完成！"
echo "======================================"
echo ""
echo "测试报告:"
echo "  - 登录接口：$OUTPUT_DIR/login-report/"
echo "  - 玩家档案：$OUTPUT_DIR/profile-report/"
echo "  - 修炼接口：$OUTPUT_DIR/cultivate-report/"
echo "  - 战斗接口：$OUTPUT_DIR/combat-report/"
echo "  - 排行榜：$OUTPUT_DIR/ranking-report/"
echo ""
echo "查看报告: open $OUTPUT_DIR/login-report/index.html"
