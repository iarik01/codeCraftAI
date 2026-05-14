FROM maven:3.9.9-eclipse-temurin-21-alpine AS build

WORKDIR /app
COPY auth-service/pom.xml ./pom.xml
COPY auth-service/src ./src

RUN mvn -q -DskipTests package

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app
RUN apk add --no-cache curl
COPY --from=build /app/target/auth-service-0.1.0.jar ./auth-service.jar

EXPOSE 8080

CMD ["java", "-jar", "auth-service.jar"]
