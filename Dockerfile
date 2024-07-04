# Use an OpenJDK image as the base image
FROM openjdk:21-jdk-slim

# Set the working directory inside the container
WORKDIR /app

# Copy the JAR file into the container
COPY target/JDA-bot-1.0-SNAPSHOT-jar-with-dependencies.jar /app/bot.jar

# Expose the health check port
EXPOSE 8000

# Command to run the JAR file
ENTRYPOINT ["java", "-jar", "/app/bot.jar"]

# Allow token to be passed as an argument
CMD ["-t", "token"]
