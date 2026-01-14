# ============================================
# 第一阶段：构建 Spring Boot 应用
# ============================================
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /build

# 先复制 pom.xml，利用 Docker 缓存加速依赖下载
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 复制源代码并构建
COPY src ./src
RUN mvn clean package -DskipTests -B

# ============================================
# 第二阶段：运行时镜像
# ============================================
FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="DingDong Team"
LABEL description="DingDong Server - Spring Boot Application"

WORKDIR /app

# 创建非 root 用户运行应用
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# 从构建阶段复制 jar 包
COPY --from=builder /build/target/*.jar app.jar

# 设置时区为上海
ENV TZ=Asia/Shanghai
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/$TZ /etc/localtime && \
    echo $TZ > /etc/timezone

# 切换到非 root 用户
USER appuser

# 暴露端口
EXPOSE 8080

# JVM 优化参数
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -XX:+UseContainerSupport"

# 启动应用
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
