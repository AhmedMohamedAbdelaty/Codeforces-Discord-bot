# Use an OpenJDK image as the base image
FROM openjdk:21-jdk-slim

# Install libfreetype6, fontconfig, and some basic fonts
RUN apt-get update && apt-get install -y \
    libfreetype6 \
    fontconfig \
    fonts-dejavu-core \
    && apt-get clean && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

RUN fc-cache -f

# Set the working directory inside the container
WORKDIR /app

# Copy the JAR file into the container
COPY target/Codeforces-Bot-1.0-SNAPSHOT-jar-with-dependencies.jar /app/bot.jar

# Expose the health check port
EXPOSE 8000

# Command to run the JAR file
ENTRYPOINT ["java", "-jar", "/app/bot.jar"]

# Allow token to be passed as an argument
CMD ["-t", "token"]
