FROM maven:3.9.9-eclipse-temurin-17 AS builder

WORKDIR /workspace

COPY backend /workspace/backend

WORKDIR /workspace/backend
RUN mvn -pl warehouse-service -am -DskipTests clean package

FROM eclipse-temurin:17-jre-jammy AS runtime

WORKDIR /app

COPY --from=builder /workspace/backend/warehouse-service/target/warehouse-service-*.jar /app/app.jar

EXPOSE 8083

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
