FROM openjdk:17-jdk-slim

WORKDIR /app

COPY backend/order-service/app.jar /app/app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
