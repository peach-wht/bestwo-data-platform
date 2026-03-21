FROM openjdk:17-jdk-slim

WORKDIR /app

COPY backend/warehouse-service/target/*.jar app.jar

EXPOSE 8083

ENTRYPOINT ["java","-jar","/app/app.jar"]