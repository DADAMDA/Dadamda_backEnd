# ---------- Build stage ----------
FROM gradle:8.10-jdk23 AS build

WORKDIR /home/gradle/src

COPY build.gradle settings.gradle ./
COPY gradle ./gradle

RUN gradle dependencies --no-daemon || true

COPY src ./src

RUN gradle bootJar --no-daemon -x test


# ---------- Run stage ----------
FROM eclipse-temurin:23-jre

WORKDIR /app

COPY --from=build /home/gradle/src/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]