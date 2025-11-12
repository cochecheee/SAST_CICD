# Use the official Gradle image to build
# This image comes with Gradle and JDK 17
FROM gradle:8.7-jdk17 AS build

# Set the working directory in the container to /app
WORKDIR /app

# Copy Gradle configuration files into the container
COPY build.gradle settings.gradle ./

# Copy the src directory into the container
COPY src ./src

# Run the Gradle command to build the application and skip tests
RUN gradle clean build -x test

# Use the official JDK image to run the application
FROM openjdk:17-jdk-slim

# Set the working directory in the container to /app
WORKDIR /app

# Copy the built jar file from the previous stage into this image
COPY --from=build /app/build/libs/social-network-v1-0.0.1-SNAPSHOT.jar app.jar

# Define the command to run the application with JVM options for Log4Shell exploitation
# WARNING: FOR LAB/EDUCATIONAL PURPOSES ONLY!
ENTRYPOINT ["java", \
    "-Dcom.sun.jndi.ldap.object.trustURLCodebase=true", \
    "-Dcom.sun.jndi.rmi.object.trustURLCodebase=true", \
    "-Djava.rmi.server.useCodebaseOnly=false", \
    "-jar", "app.jar"]
