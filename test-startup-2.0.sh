#!/bin/bash

echo "=== 修仙挂机游戏 2.0 启动测试脚本 ==="

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 检查函数
check_command() {
    if command -v "$1" &> /dev/null; then
        echo -e "${GREEN}✅ $1 已安装${NC}"
        return 0
    else
        echo -e "${RED}❌ $1 未安装${NC}"
        return 1
    fi
}

# 检查Java环境
echo "🔍 检查环境..."
if ! check_command java; then
    echo -e "${RED}请先安装Java 8或更高版本${NC}"
    exit 1
fi

# 检查Java版本
JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 8 ]; then
    echo -e "${RED}❌ Java版本过低: $JAVA_VERSION，需要Java 8或更高版本${NC}"
    exit 1
fi
echo -e "${GREEN}✅ Java版本: $(java -version 2>&1 | head -n 1)${NC}"

# 检查Maven
if ! check_command mvn; then
    echo -e "${YELLOW}⚠️  Maven未安装，将跳过编译步骤${NC}"
    SKIP_BUILD=true
else
    echo -e "${GREEN}✅ Maven已安装${NC}"
    SKIP_BUILD=false
fi

# 检查JAR文件
JAR_FILE="target/xiuxian-game.jar"
if [ ! -f "$JAR_FILE" ]; then
    if [ "$SKIP_BUILD" = true ]; then
        echo -e "${RED}❌ 未找到JAR文件 $JAR_FILE，且无法编译${NC}"
        exit 1
    else
        echo -e "${YELLOW}⚠️  未找到JAR文件，开始编译...${NC}"
        echo "📦 运行: mvn clean package"
        if mvn clean package -q; then
            echo -e "${GREEN}✅ 编译成功${NC}"
        else
            echo -e "${RED}❌ 编译失败${NC}"
            exit 1
        fi
    fi
else
    echo -e "${GREEN}✅ JAR文件存在: $JAR_FILE${NC}"
fi

# 检查端口占用
if lsof -Pi :8080 -sTCP:LISTEN -t >/dev/null 2>&1; then
    echo -e "${YELLOW}⚠️  端口8080已被占用${NC}"
    echo "占用进程:"
    lsof -Pi :8080 -sTCP:LISTEN
    echo -e "${YELLOW}启动可能会失败，请关闭占用端口的程序${NC}"
fi

# 启动测试
echo ""
echo "🚀 启动测试..."
echo "📝 启动日志将保存到 test-startup.log"
echo "🔗 访问地址: http://localhost:8080/xiuxian-game/"
echo "⏰ 测试超时: 30秒"
echo ""

# 启动应用（后台）
java -jar "$JAR_FILE" > test-startup.log 2>&1 &
APP_PID=$!

echo "📋 应用PID: $APP_PID"
echo "📄 日志文件: test-startup.log"

# 等待并检查启动
echo "⏳ 等待应用启动..."
for i in {1..30}; do
    sleep 1
    
    # 检查进程是否还在运行
    if ! ps -p $APP_PID > /dev/null 2>&1; then
        echo -e "${RED}❌ 应用进程已退出${NC}"
        echo "📋 最后20行日志:"
        tail -20 test-startup.log
        exit 1
    fi
    
    # 检查端口是否开始监听
    if lsof -Pi :8080 -sTCP:LISTEN -t >/dev/null 2>&1; then
        echo -e "${GREEN}✅ 端口8080已监听${NC}"
        break
    fi
    
    echo -n "."
done

echo ""

# 检查启动日志
if grep -q "Started XiuxianGameApplication" test-startup.log; then
    echo -e "${GREEN}✅ 应用启动成功！${NC}"
    
    # 提取启动时间
    STARTUP_TIME=$(grep "Started XiuxianGameApplication" test-startup.log | grep -o "in [0-9.]* seconds" | head -1)
    echo -e "${GREEN}🚀 启动时间: $STARTUP_TIME${NC}"
    
    # 提取端口信息
    PORT_INFO=$(grep "Tomcat started on port" test-startup.log | head -1)
    echo -e "${GREEN}📡 $PORT_INFO${NC}"
    
    echo ""
    echo -e "${GREEN}🎉 测试通过！应用已成功启动${NC}"
    echo ""
    echo "📋 访问信息:"
    echo "   🌐 游戏地址: http://localhost:8080/xiuxian-game/"
    echo "   🔧 H2控制台: http://localhost:8080/h2-console"
    echo "   📄 启动日志: test-startup.log"
    echo ""
    echo "🛑 停止应用: kill $APP_PID"
    
    # 等待用户输入
    echo ""
    echo "按回车键停止应用，或按Ctrl+C保持运行..."
    read
    
    # 停止应用
    echo "🛑 正在停止应用..."
    kill $APP_PID
    sleep 2
    
    # 强制杀死进程（如果需要）
    if ps -p $APP_PID > /dev/null 2>&1; then
        echo "⚡ 强制停止应用..."
        kill -9 $APP_PID
    fi
    
    echo -e "${GREEN}✅ 应用已停止${NC}"
    
else
    echo -e "${RED}❌ 应用启动失败${NC}"
    echo "📋 启动日志:"
    cat test-startup.log
    
    # 停止应用
    if ps -p $APP_PID > /dev/null 2>&1; then
        kill $APP_PID 2>/dev/null
    fi
    
    exit 1
fi

echo ""
echo -e "${GREEN}🎯 测试完成！${NC}"
