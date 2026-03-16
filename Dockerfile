# 使用官方 Java runtime
FROM eclipse-temurin:21-jdk

# container 內的工作目錄
WORKDIR /app

# 把 build 出來的 jar copy 進 container
COPY target/*.jar app.jar

# 開放 port
EXPOSE 8080

# 啟動 Spring Boot
ENTRYPOINT ["java","-jar","app.jar"]