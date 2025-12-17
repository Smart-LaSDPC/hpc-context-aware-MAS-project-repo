FROM maven:3.8.4-openjdk-11 AS build
WORKDIR /app

COPY pom.xml ./
RUN mvn -q -DskipTests dependency:go-offline

COPY src ./src
COPY ontologies ./ontologies
COPY APDescription.txt MTPs-Main-Container.txt ./

RUN mvn -q -DskipTests package

FROM eclipse-temurin:11-jre-jammy
WORKDIR /app

RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

COPY --from=build /app/target/MASProjectV25-0.0.1.jar /app/app.jar
COPY --from=build /app/ontologies /app/ontologies
COPY --from=build /app/APDescription.txt /app/MTPs-Main-Container.txt /app/

ENV HEALTH_PORT=8081
EXPOSE 8081

ENTRYPOINT ["java","-jar","/app/app.jar"]
