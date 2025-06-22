# Stage 1: Build JAR
FROM --platform=linux/amd64 maven:3.8.5-openjdk-17 AS builder

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

# Stage 2: Runtime image
FROM --platform=linux/amd64 openjdk:22-jdk-slim

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]