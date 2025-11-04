# Multi-stage build
# Stage 1 - build the application from source
# Use a Java base image for the container
FROM eclipse-temurin:21-jdk AS build

# Set the working directory
WORKDIR application

# Define a variable and assign the path of the jar file
ARG JAR_FILE=target/*.jar

# Copy jar file into the container and rename it
COPY ${JAR_FILE} application.jar

# Run a script to extract layers from the jar file
RUN java -Djarmode=layertools -jar application.jar extract

# Stage 2 - create the final Docker image
# Use a Java base image for the container
FROM eclipse-temurin:21-jre

# Set working directory
WORKDIR application

# Copy the 4 layers extracted by the first stage to the current docker image
# This allows Docker to reuse layers from Cache
COPY --from=build application/dependencies/ ./
COPY --from=build application/spring-boot-loader/ ./
COPY --from=build application/snapshot-dependencies/ ./
COPY --from=build application/application/ ./

# Explicilty invoke the jar launcher to start the Spring Boot application in the container
ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]

# Port the container is listening on at runtime
EXPOSE 8080