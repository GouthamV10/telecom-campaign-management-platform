# Backend Dockerfile
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app
COPY pom.xml .
COPY src ./src

RUN mvn package -DskipTests -q

FROM eclipse-temurin:21-jre

WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8081

ENV SPRING_PROFILES_ACTIVE=dev
ENV JWT_SECRET="YmFzZTY0LXNlY3JldC1rZXktZm9yLXRlbGVjb20tY2FtcGFpZ24tMTIzNDU2Nzg5MA=="

ENTRYPOINT ["java", "-jar", "app.jar"]
