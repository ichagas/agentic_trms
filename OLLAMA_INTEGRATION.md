# 🦙 Ollama Integration Guide for TRMS AI

This guide explains how to integrate Ollama (local LLM) with your TRMS AI backend using OpenAI-compatible REST API.

## 🚀 Quick Start

### 1. Install and Setup Ollama

```bash
# Run the setup script
./setup-ollama.sh

# Or manually install Ollama
curl -fsSL https://ollama.ai/install.sh | sh
ollama serve &
ollama pull llama3.1:8b
```

### 2. Configure TRMS Backend

The backend is already configured to automatically detect and use Ollama. Environment variables:

```bash
# Ollama Configuration (already set in start.sh)
export OLLAMA_ENABLED=true
export OLLAMA_BASE_URL=http://localhost:11434/v1
export OLLAMA_MODEL=llama3.1:8b
export AI_MODEL=llama3.1:8b
```

### 3. Start TRMS with Ollama

```bash
cd /Users/igorchagas/ideas/agentic_trms/backend/trms-ai-backend
./start.sh
```

## 🔧 Integration Modes

### Mode 1: Ollama Primary (Recommended)
- Uses local Ollama for all AI processing
- Fallback to mock responses if Ollama unavailable
- Best for privacy and cost efficiency

### Mode 2: OpenAI Primary with Ollama Fallback
```bash
export OLLAMA_ENABLED=false
export OPENAI_API_KEY=your-real-key
export AI_MODEL=gpt-4o-mini
```

### Mode 3: Hybrid (Load Balancing)
```bash
export OLLAMA_ENABLED=true
export OPENAI_API_KEY=your-real-key
# Backend will choose based on availability and load
```

## 🦙 Ollama OpenAI API Compatibility

Ollama provides full OpenAI API compatibility at `http://localhost:11434/v1`:

### Chat Completions
```bash
curl http://localhost:11434/v1/chat/completions \
  -H "Content-Type: application/json" \
  -d '{
    "model": "llama3.1:8b",
    "messages": [
      {"role": "system", "content": "You are a TRMS financial assistant."},
      {"role": "user", "content": "Show me USD accounts"}
    ],
    "temperature": 0.3,
    "max_tokens": 2000
  }'
```

### Function Calling (Future)
```bash
curl http://localhost:11434/v1/chat/completions \
  -H "Content-Type: application/json" \
  -d '{
    "model": "llama3.1:8b",
    "messages": [{"role": "user", "content": "Check balance for ACC-001-USD"}],
    "functions": [
      {
        "name": "checkAccountBalance",
        "description": "Check account balance",
        "parameters": {
          "type": "object",
          "properties": {
            "accountId": {"type": "string"}
          }
        }
      }
    ]
  }'
```

## 📊 Model Recommendations

### For TRMS Financial Operations:

1. **llama3.1:8b** (Recommended)
   - Best balance of performance and accuracy
   - Good financial reasoning capabilities
   - 8GB RAM requirement

2. **mistral:7b** (Fast Alternative)
   - Faster responses
   - Lower memory usage
   - Good for basic queries

3. **codellama:7b** (Code Generation)
   - Best for generating financial formulas
   - Good for complex calculations
   - Specialized for code tasks

## 🔍 Testing Integration

### 1. Test Ollama Health
```bash
curl http://localhost:11434/api/tags
```

### 2. Test OpenAI Compatibility
```bash
curl http://localhost:11434/v1/models
```

### 3. Test TRMS Backend Integration
```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "Show me all USD accounts", "sessionId": "ollama-test"}'
```

## ⚙️ Configuration Details

### Spring AI Configuration
```yaml
spring:
  ai:
    ollama:
      enabled: true
      base-url: http://localhost:11434/v1
      model: llama3.1:8b
      api-key: not-needed
```

### Environment Variables
```bash
# Ollama Settings
OLLAMA_ENABLED=true
OLLAMA_BASE_URL=http://localhost:11434/v1
OLLAMA_MODEL=llama3.1:8b

# OpenAI Fallback
OPENAI_API_KEY=sk-demo-key
OPENAI_BASE_URL=https://api.openai.com

# Model Selection
AI_MODEL=llama3.1:8b  # Uses Ollama model
# AI_MODEL=gpt-4o-mini  # Uses OpenAI model
```

## 🚨 Troubleshooting

### Ollama Not Starting
```bash
# Check if port 11434 is available
lsof -i :11434

# Start Ollama manually
ollama serve

# Check logs
tail -f ~/.ollama/logs/server.log
```

### Model Download Issues
```bash
# Re-download model
ollama pull llama3.1:8b

# Check available disk space
df -h

# List installed models
ollama list
```

### Backend Integration Issues
```bash
# Check backend logs
tail -f /Users/igorchagas/ideas/agentic_trms/backend/trms-ai-backend/logs/trms-ai-backend.log

# Test Ollama connectivity from backend
curl http://localhost:11434/api/tags
```

## 🎯 Benefits of Ollama Integration

1. **Privacy**: All AI processing happens locally
2. **Cost**: No API costs for AI operations
3. **Speed**: Low latency for local processing
4. **Reliability**: Works offline
5. **Customization**: Can fine-tune models for financial domain
6. **Compliance**: Data never leaves your infrastructure

## 🔄 Switching Between Models

```bash
# Use Ollama Llama 3.1
export AI_MODEL=llama3.1:8b

# Use Ollama Mistral
export AI_MODEL=mistral:7b

# Use OpenAI
export AI_MODEL=gpt-4o-mini
export OLLAMA_ENABLED=false

# Restart backend to apply changes
cd /Users/igorchagas/ideas/agentic_trms/backend/trms-ai-backend
./start.sh
```

Your TRMS AI system now supports both local Ollama and cloud OpenAI models with seamless switching! 🎉