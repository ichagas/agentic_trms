# TRMS AI Services Control Script - PowerShell Version
# Manages Frontend, Spring AI Backend, TRMS Mock Backend, and SWIFT Mock services
# Usage: .\trms-services.ps1 [start|stop|restart|status|logs|health]

param(
    [Parameter(Position=0)]
    [ValidateSet("start", "stop", "restart", "status", "health", "logs", "help")]
    [string]$Command = "help",

    [Parameter(Position=1)]
    [string]$Service = "all"
)

# Service Configuration
$ScriptDir = $PSScriptRoot
$FrontendDir = Join-Path $ScriptDir "frontend"
$AIBackendDir = Join-Path $ScriptDir "backend\trms-ai-backend"
$MockBackendDir = Join-Path $ScriptDir "backend\trms-mock-app"
$SwiftMockDir = Join-Path $ScriptDir "backend\swift-mock-app"

# Port Configuration
$FrontendPort = 5174
$AIBackendPort = 8080
$MockBackendPort = 8090
$SwiftMockPort = 8091

# PID Files for tracking processes
$PidDir = Join-Path $ScriptDir ".pids"
if (-not (Test-Path $PidDir)) {
    New-Item -ItemType Directory -Path $PidDir -Force | Out-Null
}
$FrontendPidFile = Join-Path $PidDir "frontend.pid"
$AIBackendPidFile = Join-Path $PidDir "ai-backend.pid"
$MockBackendPidFile = Join-Path $PidDir "mock-backend.pid"
$SwiftMockPidFile = Join-Path $PidDir "swift-mock.pid"

# Logs directory
$LogsDir = Join-Path $ScriptDir "logs"
if (-not (Test-Path $LogsDir)) {
    New-Item -ItemType Directory -Path $LogsDir -Force | Out-Null
}

# Java Configuration (adjust path for your Java installation)
$env:JAVA_HOME = if ($env:JAVA_HOME) { $env:JAVA_HOME } else {
    # Try common Windows Java locations
    if (Test-Path "C:\Program Files\Java\jdk-21") { "C:\Program Files\Java\jdk-21" }
    elseif (Test-Path "C:\Program Files\Eclipse Adoptium\jdk-21") { "C:\Program Files\Eclipse Adoptium\jdk-21" }
    else { $null }
}
if ($env:JAVA_HOME) {
    $env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
}

# Logging Functions
function Write-Info {
    param([string]$Message)
    Write-Host "[INFO] " -ForegroundColor Blue -NoNewline
    Write-Host $Message
}

function Write-Success {
    param([string]$Message)
    Write-Host "[SUCCESS] " -ForegroundColor Green -NoNewline
    Write-Host $Message
}

function Write-Warning2 {
    param([string]$Message)
    Write-Host "[WARNING] " -ForegroundColor Yellow -NoNewline
    Write-Host $Message
}

function Write-ErrorMsg {
    param([string]$Message)
    Write-Host "[ERROR] " -ForegroundColor Red -NoNewline
    Write-Host $Message
}

# Check if port is in use
function Test-PortInUse {
    param([int]$Port)

    try {
        $connection = Get-NetTCPConnection -LocalPort $Port -ErrorAction SilentlyContinue
        return $null -ne $connection
    } catch {
        return $false
    }
}

# Get process ID by port
function Get-ProcessByPort {
    param([int]$Port)

    try {
        $connection = Get-NetTCPConnection -LocalPort $Port -ErrorAction SilentlyContinue
        if ($connection) {
            return $connection.OwningProcess
        }
    } catch {
        return $null
    }
    return $null
}

# Kill process by port
function Stop-ServiceByPort {
    param(
        [int]$Port,
        [string]$ServiceName
    )

    if (Test-PortInUse -Port $Port) {
        Write-Info "Stopping $ServiceName on port $Port..."

        $processId = Get-ProcessByPort -Port $Port
        if ($processId) {
            try {
                Stop-Process -Id $processId -Force -ErrorAction SilentlyContinue
                Start-Sleep -Seconds 2

                if (Test-PortInUse -Port $Port) {
                    Write-ErrorMsg "Failed to stop $ServiceName on port $Port"
                    return $false
                } else {
                    Write-Success "$ServiceName stopped successfully"
                    return $true
                }
            } catch {
                Write-ErrorMsg "Error stopping $ServiceName: $_"
                return $false
            }
        } else {
            Write-Warning2 "Could not find process ID for port $Port"
            return $false
        }
    } else {
        Write-Info "$ServiceName is not running on port $Port"
        return $true
    }
}

# Start Frontend
function Start-Frontend {
    Write-Info "Starting React Frontend..."

    if (-not (Get-Command npm -ErrorAction SilentlyContinue)) {
        Write-ErrorMsg "npm is not installed. Please install Node.js and npm."
        return $false
    }

    if (-not (Test-Path $FrontendDir)) {
        Write-ErrorMsg "Frontend directory not found: $FrontendDir"
        return $false
    }

    Push-Location $FrontendDir

    # Install dependencies if node_modules doesn't exist
    if (-not (Test-Path "node_modules")) {
        Write-Info "Installing frontend dependencies..."
        npm install
    }

    # Start frontend in background
    $logFile = Join-Path $LogsDir "frontend.log"
    $process = Start-Process npm -ArgumentList "run", "dev" `
        -RedirectStandardOutput $logFile `
        -RedirectStandardError $logFile `
        -PassThru -NoNewWindow

    $process.Id | Out-File $FrontendPidFile

    Pop-Location

    # Wait for frontend to start
    Start-Sleep -Seconds 5
    if (Test-PortInUse -Port $FrontendPort) {
        Write-Success "Frontend started on http://localhost:$FrontendPort"
        return $true
    } else {
        Write-ErrorMsg "Frontend failed to start"
        return $false
    }
}

# Start TRMS Mock Backend
function Start-MockBackend {
    Write-Info "Starting TRMS Mock Backend..."

    if (-not (Test-Path $MockBackendDir)) {
        Write-ErrorMsg "Mock backend directory not found: $MockBackendDir"
        return $false
    }

    Push-Location $MockBackendDir

    # Start mock backend in background
    $logFile = Join-Path $LogsDir "mock-backend.log"
    $process = Start-Process mvn -ArgumentList "spring-boot:run" `
        -RedirectStandardOutput $logFile `
        -RedirectStandardError $logFile `
        -PassThru -NoNewWindow

    $process.Id | Out-File $MockBackendPidFile

    Pop-Location

    # Wait for backend to start
    Write-Info "Waiting for TRMS Mock Backend to start..."
    for ($i = 0; $i -lt 30; $i++) {
        try {
            $response = Invoke-WebRequest -Uri "http://localhost:$MockBackendPort/actuator/health" -UseBasicParsing -TimeoutSec 2 -ErrorAction SilentlyContinue
            if ($response.StatusCode -eq 200) {
                Write-Host ""
                Write-Success "TRMS Mock Backend started on http://localhost:$MockBackendPort"
                return $true
            }
        } catch {
            # Continue waiting
        }
        Start-Sleep -Seconds 2
        Write-Host "." -NoNewline
    }

    Write-Host ""
    Write-ErrorMsg "TRMS Mock Backend failed to start within 60 seconds"
    return $false
}

# Start Spring AI Backend
function Start-AIBackend {
    Write-Info "Starting Spring AI Backend..."

    if (-not (Test-Path $AIBackendDir)) {
        Write-ErrorMsg "AI backend directory not found: $AIBackendDir"
        return $false
    }

    Push-Location $AIBackendDir

    # Set AI Provider (ollama, openai, or azure)
    $env:AI_PROVIDER = if ($env:AI_PROVIDER) { $env:AI_PROVIDER } else { "ollama" }

    # Ollama configuration
    $env:OLLAMA_BASE_URL = if ($env:OLLAMA_BASE_URL) { $env:OLLAMA_BASE_URL } else { "http://localhost:11434" }
    $env:OLLAMA_MODEL = if ($env:OLLAMA_MODEL) { $env:OLLAMA_MODEL } else { "qwen3:1.7b" }

    # OpenAI configuration
    $env:OPENAI_API_KEY = if ($env:OPENAI_API_KEY) { $env:OPENAI_API_KEY } else { "sk-demo-key" }
    $env:OPENAI_BASE_URL = if ($env:OPENAI_BASE_URL) { $env:OPENAI_BASE_URL } else { "https://api.openai.com" }
    $env:AI_MODEL = if ($env:AI_MODEL) { $env:AI_MODEL } else { "gpt-4o-mini" }

    # Azure OpenAI configuration
    $env:AZURE_OPENAI_API_KEY = if ($env:AZURE_OPENAI_API_KEY) { $env:AZURE_OPENAI_API_KEY } else { "your-azure-key" }
    $env:AZURE_OPENAI_ENDPOINT = if ($env:AZURE_OPENAI_ENDPOINT) { $env:AZURE_OPENAI_ENDPOINT } else { "https://your-resource.openai.azure.com/" }
    $env:AZURE_OPENAI_DEPLOYMENT_NAME = if ($env:AZURE_OPENAI_DEPLOYMENT_NAME) { $env:AZURE_OPENAI_DEPLOYMENT_NAME } else { "gpt-4" }

    $env:SPRING_PROFILES_ACTIVE = "dev"

    Write-Info "AI Provider: $env:AI_PROVIDER"
    if ($env:AI_PROVIDER -eq "openai") {
        Write-Info "OpenAI Model: $env:AI_MODEL"
    } elseif ($env:AI_PROVIDER -eq "azure") {
        Write-Info "Azure Deployment: $env:AZURE_OPENAI_DEPLOYMENT_NAME"
    } else {
        Write-Info "Ollama Model: $env:OLLAMA_MODEL"
    }

    # Start AI backend in background
    $logFile = Join-Path $LogsDir "ai-backend.log"
    $process = Start-Process mvn -ArgumentList "spring-boot:run" `
        -RedirectStandardOutput $logFile `
        -RedirectStandardError $logFile `
        -PassThru -NoNewWindow

    $process.Id | Out-File $AIBackendPidFile

    Pop-Location

    # Wait for backend to start
    Write-Info "Waiting for Spring AI Backend to start..."
    for ($i = 0; $i -lt 30; $i++) {
        try {
            $response = Invoke-WebRequest -Uri "http://localhost:$AIBackendPort/api/chat/health" -UseBasicParsing -TimeoutSec 2 -ErrorAction SilentlyContinue
            if ($response.StatusCode -eq 200) {
                Write-Host ""
                Write-Success "Spring AI Backend started on http://localhost:$AIBackendPort"
                return $true
            }
        } catch {
            # Continue waiting
        }
        Start-Sleep -Seconds 2
        Write-Host "." -NoNewline
    }

    Write-Host ""
    Write-ErrorMsg "Spring AI Backend failed to start within 60 seconds"
    return $false
}

# Start SWIFT Mock
function Start-SwiftMock {
    Write-Info "Starting SWIFT Mock System..."

    if (-not (Test-Path $SwiftMockDir)) {
        Write-ErrorMsg "SWIFT mock directory not found: $SwiftMockDir"
        return $false
    }

    Push-Location $SwiftMockDir

    # Start SWIFT mock in background
    $logFile = Join-Path $LogsDir "swift-mock.log"
    $process = Start-Process mvn -ArgumentList "spring-boot:run" `
        -RedirectStandardOutput $logFile `
        -RedirectStandardError $logFile `
        -PassThru -NoNewWindow

    $process.Id | Out-File $SwiftMockPidFile

    Pop-Location

    # Wait for backend to start
    Write-Info "Waiting for SWIFT Mock to start..."
    for ($i = 0; $i -lt 30; $i++) {
        try {
            $response = Invoke-WebRequest -Uri "http://localhost:$SwiftMockPort/actuator/health" -UseBasicParsing -TimeoutSec 2 -ErrorAction SilentlyContinue
            if ($response.StatusCode -eq 200) {
                Write-Host ""
                Write-Success "SWIFT Mock started on http://localhost:$SwiftMockPort"
                return $true
            }
        } catch {
            # Continue waiting
        }
        Start-Sleep -Seconds 2
        Write-Host "." -NoNewline
    }

    Write-Host ""
    Write-ErrorMsg "SWIFT Mock failed to start within 60 seconds"
    return $false
}

# Start all services
function Start-AllServices {
    Write-Info "Starting TRMS AI Services..."
    Write-Host ""

    # Start services in order
    Start-MockBackend
    Start-Sleep -Seconds 5
    Start-SwiftMock
    Start-Sleep -Seconds 5
    Start-AIBackend
    Start-Sleep -Seconds 5
    Start-Frontend

    Write-Host ""
    Write-Success "All TRMS services started successfully!"
    Write-Host ""
    Write-Host "[Frontend]         http://localhost:$FrontendPort"
    Write-Host "[AI Backend]       http://localhost:$AIBackendPort"
    Write-Host "[TRMS Mock]        http://localhost:$MockBackendPort"
    Write-Host "[SWIFT Mock]       http://localhost:$SwiftMockPort"
    Write-Host ""
    Write-Host "Use '.\trms-services.ps1 status' to check service health"
    Write-Host "Use '.\trms-services.ps1 logs' to view service logs"
}

# Stop all services
function Stop-AllServices {
    Write-Info "Stopping TRMS AI Services..."

    Stop-ServiceByPort -Port $FrontendPort -ServiceName "Frontend"
    Stop-ServiceByPort -Port $AIBackendPort -ServiceName "Spring AI Backend"
    Stop-ServiceByPort -Port $SwiftMockPort -ServiceName "SWIFT Mock"
    Stop-ServiceByPort -Port $MockBackendPort -ServiceName "TRMS Mock Backend"

    # Clean up PID files
    Remove-Item $FrontendPidFile -ErrorAction SilentlyContinue
    Remove-Item $AIBackendPidFile -ErrorAction SilentlyContinue
    Remove-Item $SwiftMockPidFile -ErrorAction SilentlyContinue
    Remove-Item $MockBackendPidFile -ErrorAction SilentlyContinue

    Write-Success "All TRMS services stopped successfully!"
}

# Check service status
function Show-Status {
    Write-Host "TRMS AI Services Status:"
    Write-Host "=================================="

    # Frontend status
    if (Test-PortInUse -Port $FrontendPort) {
        Write-Host "[Frontend]        " -NoNewline
        Write-Host "RUNNING" -ForegroundColor Green -NoNewline
        Write-Host " (http://localhost:$FrontendPort)"
    } else {
        Write-Host "[Frontend]        " -NoNewline
        Write-Host "STOPPED" -ForegroundColor Red
    }

    # AI Backend status
    if (Test-PortInUse -Port $AIBackendPort) {
        try {
            $response = Invoke-WebRequest -Uri "http://localhost:$AIBackendPort/api/chat/health" -UseBasicParsing -TimeoutSec 2 -ErrorAction SilentlyContinue
            if ($response.StatusCode -eq 200) {
                Write-Host "[AI Backend]      " -NoNewline
                Write-Host "RUNNING" -ForegroundColor Green -NoNewline
                Write-Host " (http://localhost:$AIBackendPort)"
            } else {
                Write-Host "[AI Backend]      " -NoNewline
                Write-Host "STARTING" -ForegroundColor Yellow -NoNewline
                Write-Host " (port open but not ready)"
            }
        } catch {
            Write-Host "[AI Backend]      " -NoNewline
            Write-Host "STARTING" -ForegroundColor Yellow -NoNewline
            Write-Host " (port open but not ready)"
        }
    } else {
        Write-Host "[AI Backend]      " -NoNewline
        Write-Host "STOPPED" -ForegroundColor Red
    }

    # Mock Backend status
    if (Test-PortInUse -Port $MockBackendPort) {
        try {
            $response = Invoke-WebRequest -Uri "http://localhost:$MockBackendPort/actuator/health" -UseBasicParsing -TimeoutSec 2 -ErrorAction SilentlyContinue
            if ($response.StatusCode -eq 200) {
                Write-Host "[TRMS Mock]       " -NoNewline
                Write-Host "RUNNING" -ForegroundColor Green -NoNewline
                Write-Host " (http://localhost:$MockBackendPort)"
            } else {
                Write-Host "[TRMS Mock]       " -NoNewline
                Write-Host "STARTING" -ForegroundColor Yellow -NoNewline
                Write-Host " (port open but not ready)"
            }
        } catch {
            Write-Host "[TRMS Mock]       " -NoNewline
            Write-Host "STARTING" -ForegroundColor Yellow -NoNewline
            Write-Host " (port open but not ready)"
        }
    } else {
        Write-Host "[TRMS Mock]       " -NoNewline
        Write-Host "STOPPED" -ForegroundColor Red
    }

    # SWIFT Mock status
    if (Test-PortInUse -Port $SwiftMockPort) {
        try {
            $response = Invoke-WebRequest -Uri "http://localhost:$SwiftMockPort/actuator/health" -UseBasicParsing -TimeoutSec 2 -ErrorAction SilentlyContinue
            if ($response.StatusCode -eq 200) {
                Write-Host "[SWIFT Mock]      " -NoNewline
                Write-Host "RUNNING" -ForegroundColor Green -NoNewline
                Write-Host " (http://localhost:$SwiftMockPort)"
            } else {
                Write-Host "[SWIFT Mock]      " -NoNewline
                Write-Host "STARTING" -ForegroundColor Yellow -NoNewline
                Write-Host " (port open but not ready)"
            }
        } catch {
            Write-Host "[SWIFT Mock]      " -NoNewline
            Write-Host "STARTING" -ForegroundColor Yellow -NoNewline
            Write-Host " (port open but not ready)"
        }
    } else {
        Write-Host "[SWIFT Mock]      " -NoNewline
        Write-Host "STOPPED" -ForegroundColor Red
    }

    Write-Host ""

    # System resource usage
    Write-Host "System Resources:"
    Write-Host "=================================="
    $javaProcesses = @(Get-Process -Name java -ErrorAction SilentlyContinue)
    $nodeProcesses = @(Get-Process -Name node -ErrorAction SilentlyContinue)
    Write-Host "Java Processes: $($javaProcesses.Count)"
    Write-Host "Node Processes: $($nodeProcesses.Count)"

    # Log file sizes
    if (Test-Path $LogsDir) {
        Write-Host ""
        Write-Host "Log Files:"
        Write-Host "=================================="
        Get-ChildItem $LogsDir | Format-Table Name, Length, LastWriteTime -AutoSize
    }
}

# Show logs
function Show-Logs {
    param([string]$ServiceName = "all")

    switch ($ServiceName.ToLower()) {
        { $_ -in @("frontend", "front") } {
            $logFile = Join-Path $LogsDir "frontend.log"
            if (Test-Path $logFile) {
                Write-Info "[Frontend] Logs (last 50 lines):"
                Get-Content $logFile -Tail 50
            } else {
                Write-Warning2 "Frontend log file not found"
            }
        }
        { $_ -in @("ai", "ai-backend") } {
            $logFile = Join-Path $LogsDir "ai-backend.log"
            if (Test-Path $logFile) {
                Write-Info "[AI Backend] Logs (last 50 lines):"
                Get-Content $logFile -Tail 50
            } else {
                Write-Warning2 "AI Backend log file not found"
            }
        }
        { $_ -in @("mock", "mock-backend", "trms") } {
            $logFile = Join-Path $LogsDir "mock-backend.log"
            if (Test-Path $logFile) {
                Write-Info "[TRMS Mock] Logs (last 50 lines):"
                Get-Content $logFile -Tail 50
            } else {
                Write-Warning2 "TRMS Mock log file not found"
            }
        }
        { $_ -in @("swift", "swift-mock") } {
            $logFile = Join-Path $LogsDir "swift-mock.log"
            if (Test-Path $logFile) {
                Write-Info "[SWIFT Mock] Logs (last 50 lines):"
                Get-Content $logFile -Tail 50
            } else {
                Write-Warning2 "SWIFT Mock log file not found"
            }
        }
        default {
            Show-Logs -ServiceName "frontend"
            Write-Host ""
            Show-Logs -ServiceName "ai-backend"
            Write-Host ""
            Show-Logs -ServiceName "mock-backend"
            Write-Host ""
            Show-Logs -ServiceName "swift-mock"
        }
    }
}

# Restart services
function Restart-AllServices {
    Write-Info "Restarting TRMS AI Services..."
    Stop-AllServices
    Start-Sleep -Seconds 3
    Start-AllServices
}

# Health check
function Invoke-HealthCheck {
    Write-Info "Running Health Checks..."
    Write-Host "=================================="

    # Frontend health
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:$FrontendPort" -UseBasicParsing -TimeoutSec 2 -ErrorAction SilentlyContinue
        Write-Host "[Frontend]        " -NoNewline
        Write-Host "HEALTHY" -ForegroundColor Green
    } catch {
        Write-Host "[Frontend]        " -NoNewline
        Write-Host "UNHEALTHY" -ForegroundColor Red
    }

    # AI Backend health
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:$AIBackendPort/api/chat/health" -UseBasicParsing -TimeoutSec 2 -ErrorAction SilentlyContinue
        if ($response.Content -like "*TRMS AI Chat Service is running*") {
            Write-Host "[AI Backend]      " -NoNewline
            Write-Host "HEALTHY" -ForegroundColor Green
        } else {
            Write-Host "[AI Backend]      " -NoNewline
            Write-Host "UNHEALTHY" -ForegroundColor Red -NoNewline
            Write-Host " ($($response.Content))"
        }
    } catch {
        Write-Host "[AI Backend]      " -NoNewline
        Write-Host "UNHEALTHY" -ForegroundColor Red
    }

    # TRMS Mock health
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:$MockBackendPort/actuator/health" -UseBasicParsing -TimeoutSec 2 -ErrorAction SilentlyContinue
        $json = $response.Content | ConvertFrom-Json
        if ($json.status -eq "UP") {
            Write-Host "[TRMS Mock]       " -NoNewline
            Write-Host "HEALTHY" -ForegroundColor Green
        } else {
            Write-Host "[TRMS Mock]       " -NoNewline
            Write-Host "UNHEALTHY" -ForegroundColor Red
        }
    } catch {
        Write-Host "[TRMS Mock]       " -NoNewline
        Write-Host "UNHEALTHY" -ForegroundColor Red
    }

    # SWIFT Mock health
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:$SwiftMockPort/actuator/health" -UseBasicParsing -TimeoutSec 2 -ErrorAction SilentlyContinue
        $json = $response.Content | ConvertFrom-Json
        if ($json.status -eq "UP") {
            Write-Host "[SWIFT Mock]      " -NoNewline
            Write-Host "HEALTHY" -ForegroundColor Green
        } else {
            Write-Host "[SWIFT Mock]      " -NoNewline
            Write-Host "UNHEALTHY" -ForegroundColor Red
        }
    } catch {
        Write-Host "[SWIFT Mock]      " -NoNewline
        Write-Host "UNHEALTHY" -ForegroundColor Red
    }
}

# Show usage
function Show-Usage {
    Write-Host "TRMS AI Services Control Script - PowerShell Version"
    Write-Host "====================================================="
    Write-Host ""
    Write-Host "Usage: .\trms-services.ps1 [COMMAND] [OPTIONS]"
    Write-Host ""
    Write-Host "Commands:"
    Write-Host "  start          Start all TRMS services"
    Write-Host "  stop           Stop all TRMS services"
    Write-Host "  restart        Restart all TRMS services"
    Write-Host "  status         Show service status"
    Write-Host "  health         Run health checks"
    Write-Host "  logs [service] Show logs (all/frontend/ai-backend/trms/swift)"
    Write-Host "  help           Show this help message"
    Write-Host ""
    Write-Host "Examples:"
    Write-Host "  .\trms-services.ps1 start                # Start all services (default: Ollama)"
    Write-Host "  .\trms-services.ps1 stop                 # Stop all services"
    Write-Host "  .\trms-services.ps1 status               # Check status"
    Write-Host "  .\trms-services.ps1 logs frontend        # Show frontend logs"
    Write-Host "  .\trms-services.ps1 logs swift           # Show SWIFT mock logs"
    Write-Host "  .\trms-services.ps1 health               # Run health checks"
    Write-Host ""
    Write-Host "AI Provider Configuration:"
    Write-Host "  # Use Ollama (default)"
    Write-Host "  .\trms-services.ps1 start"
    Write-Host ""
    Write-Host "  # Use OpenAI"
    Write-Host '  $env:AI_PROVIDER="openai"; $env:OPENAI_API_KEY="sk-xxx"; .\trms-services.ps1 start'
    Write-Host ""
    Write-Host "  # Use custom OpenAI endpoint"
    Write-Host '  $env:AI_PROVIDER="openai"; $env:OPENAI_BASE_URL="https://custom.ai"; $env:OPENAI_API_KEY="xxx"; .\trms-services.ps1 start'
    Write-Host ""
    Write-Host "  # Use Azure OpenAI"
    Write-Host '  $env:AI_PROVIDER="azure"; $env:AZURE_OPENAI_API_KEY="xxx"; \'
    Write-Host '    $env:AZURE_OPENAI_ENDPOINT="https://your-resource.openai.azure.com/"; \'
    Write-Host '    $env:AZURE_OPENAI_DEPLOYMENT_NAME="gpt-4"; .\trms-services.ps1 start'
    Write-Host ""
    Write-Host "Services:"
    Write-Host "  [Frontend] (React):     http://localhost:$FrontendPort"
    Write-Host "  [AI Backend] (Spring):  http://localhost:$AIBackendPort"
    Write-Host "  [TRMS Mock]:            http://localhost:$MockBackendPort"
    Write-Host "  [SWIFT Mock]:           http://localhost:$SwiftMockPort"
}

# Main script logic
switch ($Command) {
    "start" {
        Start-AllServices
    }
    "stop" {
        Stop-AllServices
    }
    "restart" {
        Restart-AllServices
    }
    "status" {
        Show-Status
    }
    "health" {
        Invoke-HealthCheck
    }
    "logs" {
        Show-Logs -ServiceName $Service
    }
    default {
        Show-Usage
    }
}
