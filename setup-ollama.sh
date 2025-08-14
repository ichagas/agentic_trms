#!/bin/bash

# Ollama Setup Script for TRMS AI Integration
# This script installs and configures Ollama for use with the TRMS AI backend

echo "🦙 Setting up Ollama for TRMS AI Integration..."

# Check if Ollama is installed
if ! command -v ollama &> /dev/null; then
    echo "📦 Installing Ollama..."
    curl -fsSL https://ollama.ai/install.sh | sh
else
    echo "✅ Ollama is already installed"
fi

# Start Ollama service
echo "🚀 Starting Ollama service..."
ollama serve &
OLLAMA_PID=$!
echo "Ollama PID: $OLLAMA_PID"

# Wait for Ollama to start
echo "⏳ Waiting for Ollama to start..."
sleep 5

# Check if Ollama is running
if curl -s http://localhost:11434/api/tags > /dev/null; then
    echo "✅ Ollama is running successfully"
else
    echo "❌ Failed to start Ollama"
    exit 1
fi

# Download recommended model for financial use
echo "📥 Downloading Llama 3.1 8B model (recommended for financial tasks)..."
ollama pull llama3.1:8b

# Alternative models for different use cases
echo "📥 Downloading additional models..."
echo "   - Code Llama for code generation tasks"
ollama pull codellama:7b

echo "   - Mistral for fast responses"
ollama pull mistral:7b

# Verify models are available
echo "📋 Available models:"
ollama list

echo ""
echo "🎉 Ollama setup complete!"
echo ""
echo "Configuration for TRMS AI Backend:"
echo "- OLLAMA_BASE_URL: http://localhost:11434/v1"
echo "- OLLAMA_MODEL: llama3.1:8b"
echo "- OpenAI Compatible API: http://localhost:11434/v1/chat/completions"
echo ""
echo "To test Ollama integration:"
echo "  curl http://localhost:11434/v1/chat/completions \\"
echo "    -H 'Content-Type: application/json' \\"
echo "    -d '{"
echo "      \"model\": \"llama3.1:8b\","
echo "      \"messages\": ["
echo "        {\"role\": \"user\", \"content\": \"Hello from TRMS AI!\"}"
echo "      ]"
echo "    }'"
echo ""
echo "Your TRMS AI backend will automatically detect and use Ollama!"