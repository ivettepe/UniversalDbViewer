FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

COPY target/db-viewer-0.0.1-SNAPSHOT.jar app.jar

CMD ["java", "-jar", "app.jar"]