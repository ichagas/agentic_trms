#!/bin/bash

# TRMS AI Backend Startup Script
# Sets environment variables and starts the Spring Boot application

echo "Starting TRMS AI Backend..."

# Set JAVA_HOME if not set
if [ -z "$JAVA_HOME" ]; then
    export JAVA_HOME="/opt/homebrew/opt/openjdk@21"
    echo "JAVA_HOME set to: $JAVA_HOME"
fi

# Set AI Configuration
if [ -z "$OPENAI_API_KEY" ]; then
    export OPENAI_API_KEY="sk-demo-key"
    echo "Using demo OpenAI API key (replace with real key for production)"
fi

# Ollama Configuration (OpenAI Compatible API)
export OLLAMA_ENABLED="${OLLAMA_ENABLED:-true}"
export OLLAMA_BASE_URL="${OLLAMA_BASE_URL:-http://localhost:11434/v1}"
export OLLAMA_MODEL="${OLLAMA_MODEL:-qwen3:1.7b}"
export AI_MODEL="${AI_MODEL:-$OLLAMA_MODEL}"

# Set Spring profiles
export SPRING_PROFILES_ACTIVE="dev"

echo "Environment configuration:"
echo "- JAVA_HOME: $JAVA_HOME"
echo "- OPENAI_API_KEY: ${OPENAI_API_KEY:0:10}..."
echo "- OLLAMA_ENABLED: $OLLAMA_ENABLED"
echo "- OLLAMA_BASE_URL: $OLLAMA_BASE_URL"
echo "- OLLAMA_MODEL: $OLLAMA_MODEL"
echo "- Spring Profile: $SPRING_PROFILES_ACTIVE"
echo ""

# Start the application
echo "Starting application on port 8080..."
mvn spring-boot:run