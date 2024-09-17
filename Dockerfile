# Use an OpenJDK image as the base image
FROM openjdk:21-jdk-slim

# Install libfreetype6, fontconfig, some basic fonts, and Redis
RUN apt-get update && apt-get install -y \
    libfreetype6 \
    fontconfig \
    fonts-dejavu-core \
    redis-server \
    && apt-get clean && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

RUN fc-cache -f

# Set the working directory inside the container
WORKDIR /app

# Copy the JAR file into the container
COPY target/Codeforces-Bot-1.0-SNAPSHOT-jar-with-dependencies.jar /app/bot.jar

# Expose the health check port and Redis port
EXPOSE 8000 6379

# Create a startup script
COPY start.sh /app/start.sh
RUN chmod +x /app/start.sh

# Use the startup script as the entrypoint
ENTRYPOINT ["/app/start.sh"]

# Allow token to be passed as an argument
CMD ["-t", "token"]