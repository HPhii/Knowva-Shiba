# Stage 1: Build JDK tối ưu với jlink
FROM eclipse-temurin:17-jdk-alpine AS build

WORKDIR /opt

ENV JAVA_HOME=/opt/java/openjdk

RUN $JAVA_HOME/bin/jlink \
    --add-modules java.base,java.logging,java.sql,java.naming,java.desktop,java.management,java.security.jgss,java.instrument,\
java.xml,jdk.unsupported,java.compiler,java.transaction.xa,java.rmi,java.scripting,\
java.net.http,jdk.crypto.ec,jdk.jfr \
    --strip-debug \
    --no-man-pages \
    --no-header-files \
    --compress=2 \
    --output /opt/jdk

# Stage 2: Build JAR bằng Maven
FROM maven:3.9.6-eclipse-temurin-17-alpine AS jar-builder

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

# Stage 3: Runtime tối ưu
FROM alpine:latest

RUN apk add --no-cache bash tzdata curl

COPY --from=build /opt/jdk /opt/jdk

ENV JAVA_HOME=/opt/jdk
ENV PATH="${JAVA_HOME}/bin:${PATH}"

WORKDIR /app

COPY --from=jar-builder /app/target/flashcard-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-Xms128m", "-Xmx256m", "-XX:+UseSerialGC", "-Dspring.aop.proxy-target-class=true", "-Dspring.aop.auto=true", "-jar", "/app/app.jar"]
