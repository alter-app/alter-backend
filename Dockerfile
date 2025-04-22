FROM gradle:8.13-jdk21-alpine AS build
WORKDIR /home/gradle/project

COPY build.gradle settings.gradle ./
COPY gradle gradle/

COPY src src

RUN gradle clean build

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=build /home/gradle/project/build/libs/alter-0.0.1-SNAPSHOT.jar api.jar

EXPOSE 8080

ENTRYPOINT ["java", "-Duser.timezone=Asia/Seoul", "-jar", "api.jar"]
