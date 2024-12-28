
# Java version - lightweight
FROM amazoncorretto:17-alpine

# set working directory
WORKDIR /app

# copy image to directory
#COPY build/libs/*.jar app.jar
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar

# set port
EXPOSE 8080

# set entry point
ENTRYPOINT ["java", "-jar", "app.jar"]
