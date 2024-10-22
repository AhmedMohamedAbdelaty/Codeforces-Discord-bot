#!/bin/bash

# Start Redis server in the background
redis-server --daemonize yes

# Wait for Redis to start
while ! redis-cli ping; do
  sleep 1
done

# Start the Java application
exec java -jar /app/bot.jar "$@"