# Dockerfile
FROM openjdk:21-jdk-alpine
ARG JAR_FILE=target/digital-wallet.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
