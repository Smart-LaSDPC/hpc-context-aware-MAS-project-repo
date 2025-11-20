# Use an official Maven image with JDK 11 as the build environment
FROM maven:3.8.4-openjdk-11 AS build

# Set the working directory
WORKDIR /app

# Copy the pom.xml file and download the project dependencies
COPY pom.xml ./
RUN mvn dependency:go-offline

# Copy the project files
COPY src ./src

# Build the project
RUN mvn package

# Debug step: List the contents of the target directory
RUN ls -l target

# Use a smaller base image for the final stage
FROM openjdk:11-jre-slim

# Set the working directory
WORKDIR /app

# Copy the built jar file from the build stage
COPY --from=build /app/target/MASProjectV2-0.0.1-SNAPSHOT.jar ./MASProjectV2.jar

# Specify the command to run the application
ENTRYPOINT ["java", "-jar", "MASProjectV2.jar"]
