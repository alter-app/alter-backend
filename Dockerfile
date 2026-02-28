FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY build/libs/alter-0.0.1-SNAPSHOT.jar api.jar

EXPOSE 8080

ENTRYPOINT ["java", "-Duser.timezone=Asia/Seoul", "-jar", "api.jar"]
