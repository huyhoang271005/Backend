FROM maven:3.9.6-eclipse-temurin-21-alpine AS build

WORKDIR /build

COPY pom.xml .

RUN mvn dependency:go-offline -B

COPY src ./src

RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Lấy file JAR đã được đóng gói từ "Giai đoạn 1" (build stage) qua đây
COPY --from=build /build/target/CommerceBackend-*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]