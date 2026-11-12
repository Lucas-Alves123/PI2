FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -q

COPY src ./src
RUN mvn package -DskipTests=false -Dfailsafe.skip=true -q

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN addgroup -S rotavital && adduser -S rotavital -G rotavital
USER rotavital

COPY --from=build /app/target/rotavital.jar rotavital.jar

EXPOSE 8080

HEALTHCHECK --interval=15s --timeout=5s --retries=5 \
  CMD wget -qO- http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "rotavital.jar"]
