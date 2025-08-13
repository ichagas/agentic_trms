#!/bin/bash

# TRMS AI Backend Startup Script
# Sets environment variables and starts the Spring Boot application

echo "Starting TRMS AI Backend..."

# Set JAVA_HOME if not set
if [ -z "$JAVA_HOME" ]; then
    export JAVA_HOME="/opt/homebrew/opt/openjdk@21"
    echo "JAVA_HOME set to: $JAVA_HOME"
fi

# Set OpenAI API Key (use environment variable or demo key for testing)
if [ -z "$OPENAI_API_KEY" ]; then
    export OPENAI_API_KEY="sk-demo-key"
    echo "Using demo OpenAI API key (replace with real key for production)"
fi

# Set Spring profiles
export SPRING_PROFILES_ACTIVE="dev"

echo "Environment configuration:"
echo "- JAVA_HOME: $JAVA_HOME"
echo "- OPENAI_API_KEY: ${OPENAI_API_KEY:0:10}..."
echo "- Spring Profile: $SPRING_PROFILES_ACTIVE"
echo ""

# Start the application
echo "Starting application on port 8080..."
mvn spring-boot:run