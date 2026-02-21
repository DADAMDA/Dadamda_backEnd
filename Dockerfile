# 1. Build stage
FROM gradle:8.10-jdk23 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build --no-daemon -x test

# 2. Run stage
# jre-jammy 태그 대신 확실히 존재하는 태그로 변경
FROM eclipse-temurin:23-jdk
WORKDIR /app
COPY --from=build /home/gradle/src/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]