# 使用官方OpenJDK 8 JRE slim镜像作为基础镜像
FROM openjdk:8-jre-slim

# 设置维护者信息
LABEL maintainer="xiuxian-game-team"
LABEL version="1.1.0"
LABEL description="xiuxian挂机游戏 - 完善与扩展版本"

# 安装必要的工具
RUN apt-get update && apt-get install -y \
    curl \
    && rm -rf /var/lib/apt/lists/*

# 设置时区为上海
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 创建应用目录和日志目录
WORKDIR /app
RUN mkdir -p /app/logs

# 创建非root用户
RUN groupadd -r xiuxian && useradd -r -g xiuxian xiuxian
RUN chown -R xiuxian:xiuxian /app

# 复制JAR文件到容器中
COPY target/xiuxian-game-*.jar app.jar

# 暴露端口8081
EXPOSE 8081

# 设置JVM参数和应用参数（增加内存以支持新功能）
ENV JAVA_OPTS="-Xms512m -Xmx1536m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/app/logs/heapdump.hprof -Xlog:gc*:file=/app/logs/gc.log:time,uptime:filecount=5,filesize=10M -XX:MaxDirectMemorySize=256m -Djava.awt.headless=true"
ENV SERVER_PORT=8081
ENV SPRING_PROFILES_ACTIVE=prod

# 切换到非root用户
USER xiuxian

# 启动应用
ENTRYPOINT exec java $JAVA_OPTS \
  -Dserver.port=$SERVER_PORT \
  -Dspring.profiles.active=$SPRING_PROFILES_ACTIVE \
  -Dlogging.file.path=/app/logs \
  -Djava.security.egd=file:/dev/./urandom \
  -jar app.jar

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:$SERVER_PORT/actuator/health || exit 1