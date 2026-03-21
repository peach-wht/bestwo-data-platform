FROM openjdk:17-jdk-slim

WORKDIR /app

COPY backend/warehouse-service/app.jar /app/app.jar

EXPOSE 8083

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
