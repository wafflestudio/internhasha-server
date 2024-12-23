
# Java version
FROM openjdk:17

# set working directory
WORKDIR /app

# copy image to directory
COPY build/libs/*.jar app.jar

# set port
EXPOSE 8080

# set entry point
ENTRYPOINT ["java", "-jar", "app.jar"]