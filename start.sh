#!/bin/bash

# xiuxian挂机游戏启动脚本
# 支持Linux/macOS环境下的便捷启动、停止和重启操作

# ==================== 配置区域 ====================
# JVM参数配置
JAVA_OPTS="-Xms128m -Xmx256m -XX:+UseG1GC -Dfile.encoding=UTF-8"
# 应用端口配置
APP_PORT=6000
# 日志目录
LOG_DIR="./logs"
# 应用名称
APP_NAME="xiuxian-game"
# JAR文件路径
JAR_FILE="./target/xiuxian-game.jar"
# PID文件路径
PID_FILE="./$APP_NAME.pid"
# ==================== 配置区域结束 ====================

# 创建日志目录
mkdir -p "$LOG_DIR"

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 日志函数
log_info() {
    echo -e "${GREEN}[INFO]${NC} $(date '+%Y-%m-%d %H:%M:%S') - $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $(date '+%Y-%m-%d %H:%M:%S') - $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $(date '+%Y-%m-%d %H:%M:%S') - $1"
}

# 检查Java环境
check_java() {
    if ! command -v java &> /dev/null; then
        log_error "未检测到Java环境，请先安装Java 8或更高版本"
        exit 1
    fi
    
    JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}')
    log_info "检测到Java版本: $JAVA_VERSION"
}

# 编译项目
build_project() {
    log_info "开始编译项目..."
    
    if ! command -v mvn &> /dev/null; then
        log_error "未检测到Maven，请先安装Maven"
        exit 1
    fi
    
    mvn clean package -DskipTests
    
    if [ $? -eq 0 ]; then
        log_info "项目编译成功"
    else
        log_error "项目编译失败"
        exit 1
    fi
}

# 启动应用
start_app() {
    # 检查是否已在运行
    if [ -f "$PID_FILE" ]; then
        PID=$(cat "$PID_FILE")
        if ps -p "$PID" > /dev/null; then
            log_warn "应用已在运行 (PID: $PID)"
            return
        else
            # 清理无效的PID文件
            rm -f "$PID_FILE"
        fi
    fi
    
    # 检查端口占用
    if lsof -Pi :$APP_PORT -sTCP:LISTEN -t >/dev/null; then
        log_error "端口 $APP_PORT 已被占用"
        exit 1
    fi
    
    log_info "正在启动应用..."
    
    # 启动应用
    nohup java $JAVA_OPTS \
        -Dserver.port=$APP_PORT \
        -Dlogging.file.name="$LOG_DIR/$APP_NAME.log" \
        -jar "$JAR_FILE" \
        > "$LOG_DIR/startup.log" 2>&1 &
    
    APP_PID=$!
    echo $APP_PID > "$PID_FILE"
    
    # 等待应用启动
    log_info "等待应用启动..."
    sleep 10
    
    # 检查启动状态
    if ps -p "$APP_PID" > /dev/null; then
        log_info "应用启动成功 (PID: $APP_PID)"
        log_info "应用查看日志: tail -f $LOG_DIR/$APP_NAME.log"
        log_info "💡 启动完成后，请访问: http://localhost:$APP_PORT/login.html"
    else
        log_error "应用启动失败，请检查日志: $LOG_DIR/startup.log"
        rm -f "$PID_FILE"
        exit 1
    fi
}

# 停止应用
stop_app() {
    if [ ! -f "$PID_FILE" ]; then
        log_warn "未找到PID文件，应用可能未运行"
        return
    fi
    
    PID=$(cat "$PID_FILE")
    
    if ps -p "$PID" > /dev/null; then
        log_info "正在停止应用 (PID: $PID)..."
        kill "$PID"
        
        # 等待应用停止
        TIMEOUT=30
        while [ $TIMEOUT -gt 0 ] && ps -p "$PID" > /dev/null; do
            sleep 1
            ((TIMEOUT--))
        done
        
        if ps -p "$PID" > /dev/null; then
            log_warn "正常停止超时，强制终止应用..."
            kill -9 "$PID"
        fi
        
        log_info "应用已停止"
    else
        log_warn "进程 $PID 不存在，应用可能已停止"
    fi
    
    rm -f "$PID_FILE"
}

# 重启应用
restart_app() {
    log_info "正在重启应用..."
    stop_app
    sleep 3
    start_app
}

# 查看应用状态
status_app() {
    if [ -f "$PID_FILE" ]; then
        PID=$(cat "$PID_FILE")
        if ps -p "$PID" > /dev/null; then
            log_info "应用正在运行 (PID: $PID)"
            echo "端口信息:"
            netstat -tuln | grep :$APP_PORT
        else
            log_warn "PID文件存在但应用未运行"
        fi
    else
        log_info "应用未运行"
    fi
}

# 显示帮助信息
show_help() {
    echo "==================== xiuxian挂机游戏启动脚本 ===================="
    echo "使用方法: ./start.sh [选项]"
    echo ""
    echo "选项:"
    echo "  start     启动应用"
    echo "  stop      停止应用"
    echo "  restart   重启应用"
    echo "  status    查看应用状态"
    echo "  build     编译项目"
    echo "  help      显示此帮助信息"
    echo ""
    echo "配置信息:"
    echo "  应用名称: $APP_NAME"
    echo "  应用端口: $APP_PORT"
    echo "  JVM参数: $JAVA_OPTS"
    echo "  日志目录: $LOG_DIR"
    echo "  JAR文件: $JAR_FILE"
    echo ""
    echo "注意事项:"
    echo "  1. 首次运行请先执行编译: ./start.sh build"
    echo "  2. 启动前请确保端口 $APP_PORT 未被占用"
    echo "  3. 应用日志位于: $LOG_DIR/$APP_NAME.log"
    echo "  4. 启动后访问: http://localhost:$APP_PORT/login.html"
    echo "============================================================"
}

# 主程序入口
main() {
    case "$1" in
        start)
            check_java
            start_app
            ;;
        stop)
            stop_app
            ;;
        restart)
            check_java
            restart_app
            ;;
        status)
            status_app
            ;;
        build)
            check_java
            build_project
            ;;
        help|"")
            show_help
            ;;
        *)
            log_error "未知选项: $1"
            show_help
            exit 1
            ;;
    esac
}

# 执行主程序
main "$@"