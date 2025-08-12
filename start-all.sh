#!/bin/bash

echo "Starting TRMS POC Services..."

# Function to check if port is in use
check_port() {
    if lsof -Pi :$1 -sTCP:LISTEN -t >/dev/null ; then
        echo "Port $1 is already in use"
        return 1
    else
        return 0
    fi
}

# Check ports
echo "Checking ports..."
check_port 8080 || exit 1
check_port 8081 || exit 1
check_port 5173 || exit 1

echo "All ports are available. Starting services..."

# Start TRMS Mock System
echo "Starting TRMS Mock System on port 8080..."
cd backend/trms-mock
mvn spring-boot:run &
TRMS_PID=$!
cd ../..

# Wait a moment for the first service to start
sleep 10

# Start AI Service
echo "Starting AI Service on port 8081..."
cd backend/trms-ai
mvn spring-boot:run &
AI_PID=$!
cd ../..

# Wait a moment for the AI service to start
sleep 10

# Start React Frontend
echo "Starting React Frontend on port 5173..."
cd frontend/trms-chat
pnpm run dev --host &
REACT_PID=$!
cd ../..

echo ""
echo "All services started successfully!"
echo ""
echo "Services:"
echo "- TRMS Mock System: http://localhost:8080"
echo "- AI Service: http://localhost:8081"
echo "- React Frontend: http://localhost:5173"
echo ""
echo "Open http://localhost:5173 in your browser to use the application"
echo ""
echo "To stop all services, run: kill $TRMS_PID $AI_PID $REACT_PID"

# Wait for user input to stop
read -p "Press Enter to stop all services..."

# Stop all services
echo "Stopping services..."
kill $TRMS_PID $AI_PID $REACT_PID 2>/dev/null

echo "All services stopped."

