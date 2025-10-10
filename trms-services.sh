#!/bin/bash

# TRMS AI Services Control Script
# Manages Frontend, Spring AI Backend, TRMS Mock Backend, and SWIFT Mock services
# Usage: ./trms-services.sh [start|stop|restart|status|logs]

set -e

# Service Configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
FRONTEND_DIR="$SCRIPT_DIR/frontend"
AI_BACKEND_DIR="$SCRIPT_DIR/backend/trms-ai-backend"
MOCK_BACKEND_DIR="$SCRIPT_DIR/backend/trms-mock-app"
SWIFT_MOCK_DIR="$SCRIPT_DIR/backend/swift-mock-app"

# Port Configuration
FRONTEND_PORT=5174
AI_BACKEND_PORT=8080
MOCK_BACKEND_PORT=8090
SWIFT_MOCK_PORT=8091

# PID Files for tracking processes
PID_DIR="$SCRIPT_DIR/.pids"
mkdir -p "$PID_DIR"
FRONTEND_PID_FILE="$PID_DIR/frontend.pid"
AI_BACKEND_PID_FILE="$PID_DIR/ai-backend.pid"
MOCK_BACKEND_PID_FILE="$PID_DIR/mock-backend.pid"
SWIFT_MOCK_PID_FILE="$PID_DIR/swift-mock.pid"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Java Configuration
export JAVA_HOME="${JAVA_HOME:-/opt/homebrew/opt/openjdk@21}"
export PATH="$JAVA_HOME/bin:$PATH"

# Logging
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if port is in use
is_port_in_use() {
    local port=$1
    lsof -ti:$port > /dev/null 2>&1
}

# Kill process by port
kill_by_port() {
    local port=$1
    local service_name=$2
    
    if is_port_in_use $port; then
        log_info "Stopping $service_name on port $port..."
        lsof -ti:$port | xargs kill -9 2>/dev/null || true
        sleep 2
        if is_port_in_use $port; then
            log_error "Failed to stop $service_name on port $port"
            return 1
        else
            log_success "$service_name stopped successfully"
        fi
    else
        log_info "$service_name is not running on port $port"
    fi
}

# Start Frontend
start_frontend() {
    log_info "Starting React Frontend..."
    
    if ! command -v npm &> /dev/null; then
        log_error "npm is not installed. Please install Node.js and npm."
        return 1
    fi
    
    if [ ! -d "$FRONTEND_DIR" ]; then
        log_error "Frontend directory not found: $FRONTEND_DIR"
        return 1
    fi
    
    cd "$FRONTEND_DIR"
    
    # Install dependencies if node_modules doesn't exist
    if [ ! -d "node_modules" ]; then
        log_info "Installing frontend dependencies..."
        npm install
    fi
    
    # Start frontend in background
    nohup npm run dev > "$SCRIPT_DIR/logs/frontend.log" 2>&1 &
    echo $! > "$FRONTEND_PID_FILE"
    
    # Wait for frontend to start
    sleep 5
    if is_port_in_use $FRONTEND_PORT; then
        log_success "Frontend started on http://localhost:$FRONTEND_PORT"
    else
        log_error "Frontend failed to start"
        return 1
    fi
}

# Start TRMS Mock Backend
start_mock_backend() {
    log_info "Starting TRMS Mock Backend..."
    
    if [ ! -d "$MOCK_BACKEND_DIR" ]; then
        log_error "Mock backend directory not found: $MOCK_BACKEND_DIR"
        return 1
    fi
    
    cd "$MOCK_BACKEND_DIR"
    
    # Start mock backend in background
    nohup mvn spring-boot:run > "$SCRIPT_DIR/logs/mock-backend.log" 2>&1 &
    echo $! > "$MOCK_BACKEND_PID_FILE"
    
    # Wait for backend to start
    log_info "Waiting for TRMS Mock Backend to start..."
    for i in {1..30}; do
        if curl -s http://localhost:$MOCK_BACKEND_PORT/actuator/health > /dev/null 2>&1; then
            log_success "TRMS Mock Backend started on http://localhost:$MOCK_BACKEND_PORT"
            return 0
        fi
        sleep 2
        echo -n "."
    done
    echo ""
    log_error "TRMS Mock Backend failed to start within 60 seconds"
    return 1
}

# Start Spring AI Backend
start_ai_backend() {
    log_info "Starting Spring AI Backend..."
    
    if [ ! -d "$AI_BACKEND_DIR" ]; then
        log_error "AI backend directory not found: $AI_BACKEND_DIR"
        return 1
    fi
    
    cd "$AI_BACKEND_DIR"
    
    # Set AI configuration
    export OLLAMA_ENABLED="${OLLAMA_ENABLED:-true}"
    export OLLAMA_BASE_URL="${OLLAMA_BASE_URL:-http://localhost:11434}"
    export OLLAMA_MODEL="${OLLAMA_MODEL:-qwen3:1.7b}"
    export AI_MODEL="${AI_MODEL:-$OLLAMA_MODEL}"
    export OPENAI_API_KEY="${OPENAI_API_KEY:-sk-demo-key}"
    export SPRING_PROFILES_ACTIVE="dev"
    
    # Start AI backend in background
    nohup mvn spring-boot:run > "$SCRIPT_DIR/logs/ai-backend.log" 2>&1 &
    echo $! > "$AI_BACKEND_PID_FILE"
    
    # Wait for backend to start
    log_info "Waiting for Spring AI Backend to start..."
    for i in {1..30}; do
        if curl -s http://localhost:$AI_BACKEND_PORT/api/chat/health > /dev/null 2>&1; then
            log_success "Spring AI Backend started on http://localhost:$AI_BACKEND_PORT"
            return 0
        fi
        sleep 2
        echo -n "."
    done
    echo ""
    log_error "Spring AI Backend failed to start within 60 seconds"
    return 1
}

# Start SWIFT Mock
start_swift_mock() {
    log_info "Starting SWIFT Mock System..."

    if [ ! -d "$SWIFT_MOCK_DIR" ]; then
        log_error "SWIFT mock directory not found: $SWIFT_MOCK_DIR"
        return 1
    fi

    cd "$SWIFT_MOCK_DIR"

    # Start SWIFT mock in background
    nohup mvn spring-boot:run > "$SCRIPT_DIR/logs/swift-mock.log" 2>&1 &
    echo $! > "$SWIFT_MOCK_PID_FILE"

    # Wait for backend to start
    log_info "Waiting for SWIFT Mock to start..."
    for i in {1..30}; do
        if curl -s http://localhost:$SWIFT_MOCK_PORT/actuator/health > /dev/null 2>&1; then
            log_success "SWIFT Mock started on http://localhost:$SWIFT_MOCK_PORT"
            return 0
        fi
        sleep 2
        echo -n "."
    done
    echo ""
    log_error "SWIFT Mock failed to start within 60 seconds"
    return 1
}

# Start all services
start_services() {
    log_info "🚀 Starting TRMS AI Services..."

    # Create logs directory
    mkdir -p "$SCRIPT_DIR/logs"

    # Start services in order
    start_mock_backend
    sleep 5
    start_swift_mock
    sleep 5
    start_ai_backend
    sleep 5
    start_frontend

    echo ""
    log_success "🎉 All TRMS services started successfully!"
    echo ""
    echo "📱 Frontend:         http://localhost:$FRONTEND_PORT"
    echo "🤖 AI Backend:       http://localhost:$AI_BACKEND_PORT"
    echo "🏦 TRMS Mock:        http://localhost:$MOCK_BACKEND_PORT"
    echo "💳 SWIFT Mock:       http://localhost:$SWIFT_MOCK_PORT"
    echo ""
    echo "📋 Use './trms-services.sh status' to check service health"
    echo "📋 Use './trms-services.sh logs' to view service logs"
}

# Stop all services
stop_services() {
    log_info "🛑 Stopping TRMS AI Services..."

    kill_by_port $FRONTEND_PORT "Frontend"
    kill_by_port $AI_BACKEND_PORT "Spring AI Backend"
    kill_by_port $SWIFT_MOCK_PORT "SWIFT Mock"
    kill_by_port $MOCK_BACKEND_PORT "TRMS Mock Backend"

    # Clean up PID files
    rm -f "$FRONTEND_PID_FILE" "$AI_BACKEND_PID_FILE" "$SWIFT_MOCK_PID_FILE" "$MOCK_BACKEND_PID_FILE"

    log_success "🎉 All TRMS services stopped successfully!"
}

# Check service status
check_status() {
    echo "🔍 TRMS AI Services Status:"
    echo "=================================="
    
    # Frontend status
    if is_port_in_use $FRONTEND_PORT; then
        echo -e "📱 Frontend:      ${GREEN}RUNNING${NC} (http://localhost:$FRONTEND_PORT)"
    else
        echo -e "📱 Frontend:      ${RED}STOPPED${NC}"
    fi
    
    # AI Backend status
    if is_port_in_use $AI_BACKEND_PORT; then
        if curl -s http://localhost:$AI_BACKEND_PORT/api/chat/health > /dev/null 2>&1; then
            echo -e "🤖 AI Backend:    ${GREEN}RUNNING${NC} (http://localhost:$AI_BACKEND_PORT)"
        else
            echo -e "🤖 AI Backend:    ${YELLOW}STARTING${NC} (port open but not ready)"
        fi
    else
        echo -e "🤖 AI Backend:    ${RED}STOPPED${NC}"
    fi
    
    # Mock Backend status
    if is_port_in_use $MOCK_BACKEND_PORT; then
        if curl -s http://localhost:$MOCK_BACKEND_PORT/actuator/health > /dev/null 2>&1; then
            echo -e "🏦 TRMS Mock:     ${GREEN}RUNNING${NC} (http://localhost:$MOCK_BACKEND_PORT)"
        else
            echo -e "🏦 TRMS Mock:     ${YELLOW}STARTING${NC} (port open but not ready)"
        fi
    else
        echo -e "🏦 TRMS Mock:     ${RED}STOPPED${NC}"
    fi

    # SWIFT Mock status
    if is_port_in_use $SWIFT_MOCK_PORT; then
        if curl -s http://localhost:$SWIFT_MOCK_PORT/actuator/health > /dev/null 2>&1; then
            echo -e "💳 SWIFT Mock:    ${GREEN}RUNNING${NC} (http://localhost:$SWIFT_MOCK_PORT)"
        else
            echo -e "💳 SWIFT Mock:    ${YELLOW}STARTING${NC} (port open but not ready)"
        fi
    else
        echo -e "💳 SWIFT Mock:    ${RED}STOPPED${NC}"
    fi

    echo ""
    
    # System resource usage
    echo "💻 System Resources:"
    echo "=================================="
    echo "Java Processes: $(pgrep -f java | wc -l)"
    echo "Node Processes: $(pgrep -f node | wc -l)"
    
    # Log file sizes
    if [ -d "$SCRIPT_DIR/logs" ]; then
        echo ""
        echo "📄 Log Files:"
        echo "=================================="
        ls -lh "$SCRIPT_DIR/logs"/ 2>/dev/null || echo "No log files found"
    fi
}

# Show logs
show_logs() {
    local service=${1:-all}
    
    case $service in
        frontend|front)
            if [ -f "$SCRIPT_DIR/logs/frontend.log" ]; then
                log_info "📱 Frontend Logs (last 50 lines):"
                tail -50 "$SCRIPT_DIR/logs/frontend.log"
            else
                log_warning "Frontend log file not found"
            fi
            ;;
        ai|ai-backend)
            if [ -f "$SCRIPT_DIR/logs/ai-backend.log" ]; then
                log_info "🤖 AI Backend Logs (last 50 lines):"
                tail -50 "$SCRIPT_DIR/logs/ai-backend.log"
            else
                log_warning "AI Backend log file not found"
            fi
            ;;
        mock|mock-backend|trms)
            if [ -f "$SCRIPT_DIR/logs/mock-backend.log" ]; then
                log_info "🏦 TRMS Mock Logs (last 50 lines):"
                tail -50 "$SCRIPT_DIR/logs/mock-backend.log"
            else
                log_warning "TRMS Mock log file not found"
            fi
            ;;
        swift|swift-mock)
            if [ -f "$SCRIPT_DIR/logs/swift-mock.log" ]; then
                log_info "💳 SWIFT Mock Logs (last 50 lines):"
                tail -50 "$SCRIPT_DIR/logs/swift-mock.log"
            else
                log_warning "SWIFT Mock log file not found"
            fi
            ;;
        all|*)
            show_logs frontend
            echo ""
            show_logs ai-backend
            echo ""
            show_logs mock-backend
            echo ""
            show_logs swift-mock
            ;;
    esac
}

# Restart services
restart_services() {
    log_info "🔄 Restarting TRMS AI Services..."
    stop_services
    sleep 3
    start_services
}

# Health check
health_check() {
    log_info "🏥 Running Health Checks..."
    echo "=================================="
    
    # Frontend health
    if curl -s http://localhost:$FRONTEND_PORT > /dev/null 2>&1; then
        echo -e "📱 Frontend:      ${GREEN}HEALTHY${NC}"
    else
        echo -e "📱 Frontend:      ${RED}UNHEALTHY${NC}"
    fi
    
    # AI Backend health
    ai_health=$(curl -s http://localhost:$AI_BACKEND_PORT/api/chat/health 2>/dev/null || echo "ERROR")
    if [ "$ai_health" = "TRMS AI Chat Service is running" ]; then
        echo -e "🤖 AI Backend:    ${GREEN}HEALTHY${NC}"
    else
        echo -e "🤖 AI Backend:    ${RED}UNHEALTHY${NC} ($ai_health)"
    fi
    
    # TRMS Mock health
    mock_health=$(curl -s http://localhost:$MOCK_BACKEND_PORT/actuator/health 2>/dev/null | grep -o '"status":"UP"' || echo "ERROR")
    if [ "$mock_health" = '"status":"UP"' ]; then
        echo -e "🏦 TRMS Mock:     ${GREEN}HEALTHY${NC}"
    else
        echo -e "🏦 TRMS Mock:     ${RED}UNHEALTHY${NC}"
    fi

    # SWIFT Mock health
    swift_health=$(curl -s http://localhost:$SWIFT_MOCK_PORT/actuator/health 2>/dev/null | grep -o '"status":"UP"' || echo "ERROR")
    if [ "$swift_health" = '"status":"UP"' ]; then
        echo -e "💳 SWIFT Mock:    ${GREEN}HEALTHY${NC}"
    else
        echo -e "💳 SWIFT Mock:    ${RED}UNHEALTHY${NC}"
    fi
}

# Show usage
show_usage() {
    echo "TRMS AI Services Control Script"
    echo "================================"
    echo ""
    echo "Usage: $0 [COMMAND] [OPTIONS]"
    echo ""
    echo "Commands:"
    echo "  start          Start all TRMS services"
    echo "  stop           Stop all TRMS services"
    echo "  restart        Restart all TRMS services"
    echo "  status         Show service status"
    echo "  health         Run health checks"
    echo "  logs [service] Show logs (all/frontend/ai-backend/trms/swift)"
    echo "  help           Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0 start                # Start all services"
    echo "  $0 stop                 # Stop all services"
    echo "  $0 status               # Check status"
    echo "  $0 logs frontend        # Show frontend logs"
    echo "  $0 logs swift           # Show SWIFT mock logs"
    echo "  $0 health               # Run health checks"
    echo ""
    echo "Services:"
    echo "  📱 Frontend (React):     http://localhost:$FRONTEND_PORT"
    echo "  🤖 AI Backend (Spring):  http://localhost:$AI_BACKEND_PORT"
    echo "  🏦 TRMS Mock:            http://localhost:$MOCK_BACKEND_PORT"
    echo "  💳 SWIFT Mock:           http://localhost:$SWIFT_MOCK_PORT"
}

# Main script logic
case "${1:-help}" in
    start)
        start_services
        ;;
    stop)
        stop_services
        ;;
    restart)
        restart_services
        ;;
    status)
        check_status
        ;;
    health)
        health_check
        ;;
    logs)
        show_logs "$2"
        ;;
    help|--help|-h)
        show_usage
        ;;
    *)
        log_error "Unknown command: $1"
        echo ""
        show_usage
        exit 1
        ;;
esac