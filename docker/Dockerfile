FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# 安装字体（用于EasyExcel导出）
RUN apk add --no-cache fontconfig ttf-dejavu

# 复制本地打包好的JAR
COPY fitness-admin-app/target/fitness-admin-app-*.jar app.jar

# 创建日志目录
RUN mkdir -p /var/log/fitness-admin

EXPOSE 8080

ENV JAVA_OPTS="-Xms512m -Xmx1024m -Djava.security.egd=file:/dev/./urandom"
ENV SPRING_PROFILES_ACTIVE=prod

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar --spring.profiles.active=$SPRING_PROFILES_ACTIVE"]
