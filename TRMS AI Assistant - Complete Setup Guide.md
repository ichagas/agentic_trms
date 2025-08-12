# TRMS AI Assistant - Complete Setup Guide

This comprehensive guide will walk you through setting up the TRMS AI Assistant Proof of Concept from scratch. The project consists of three main components: a mocked legacy TRMS system built with Spring Boot, an AI integration service using Spring AI, and a modern React frontend.

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Project Overview](#project-overview)
3. [Initial Setup](#initial-setup)
4. [Backend Setup](#backend-setup)
5. [Frontend Setup](#frontend-setup)
6. [Running the Application](#running-the-application)
7. [Testing the System](#testing-the-system)
8. [Troubleshooting](#troubleshooting)
9. [Development Workflow](#development-workflow)

## Prerequisites

Before you begin, ensure you have the following software installed on your system:

### Required Software

**Java Development Kit (JDK) 17 or higher**
- Download from [Oracle JDK](https://www.oracle.com/java/technologies/downloads/) or [OpenJDK](https://openjdk.org/)
- Verify installation: `java -version`
- Expected output should show version 17 or higher

**Apache Maven 3.8 or higher**
- Download from [Apache Maven](https://maven.apache.org/download.cgi)
- Verify installation: `mvn -version`
- Expected output should show Maven version 3.8+ and Java version 17+

**Node.js 20 or higher**
- Download from [Node.js official website](https://nodejs.org/)
- Verify installation: `node --version`
- Expected output should show version 20 or higher

**pnpm Package Manager**
- Install globally: `npm install -g pnpm`
- Verify installation: `pnpm --version`
- Alternative: You can use npm or yarn, but pnpm is recommended for better performance

### Optional but Recommended

**Git**
- For version control and cloning the repository
- Download from [Git official website](https://git-scm.com/)

**IDE or Text Editor**
- IntelliJ IDEA (recommended for Java development)
- Visual Studio Code (excellent for React development)
- Eclipse IDE

## Project Overview

The TRMS AI Assistant is structured as a microservices architecture with three distinct components:

### Architecture Components

1. **Legacy TRMS Mock System (Port 8080)**
   - Spring Boot application
   - H2 in-memory database
   - RESTful API for account and transaction management
   - Sample data pre-loaded for demonstration

2. **AI Integration Service (Port 8081)**
   - Spring Boot application with Spring AI integration
   - Mocked AI responses (ready for real LLM integration)
   - Function calling capabilities for TRMS operations
   - Chat API for natural language interactions

3. **React Frontend (Port 5173)**
   - Modern React application with Vite
   - Tailwind CSS for styling
   - shadcn/ui components
   - Google-like interface with chat functionality

### Technology Stack

| Component | Technologies |
|-----------|-------------|
| Backend | Spring Boot 3.2+, Spring AI 0.8.0, Maven, H2 Database |
| Frontend | React 18, Vite, Tailwind CSS, shadcn/ui, Framer Motion |
| Development | Java 17, Node.js 20, pnpm |

## Initial Setup

### Step 1: Create Project Directory

Create a new directory for your project and navigate to it:

```bash
mkdir trms-ai-assistant
cd trms-ai-assistant
```

### Step 2: Download or Clone the Project

If you have the project files, extract them to your project directory. The final structure should look like this:

```
trms-ai-assistant/
├── backend/
│   ├── trms-mock/          # Legacy TRMS System
│   └── trms-ai/            # AI Integration Service
├── frontend/
│   └── trms-chat/          # React Frontend
├── start-all.sh            # Startup script
├── README.md
├── SETUP.md               # This file
└── DEPLOYMENT.md
```

## Backend Setup

### Setting Up the Legacy TRMS Mock System

Navigate to the TRMS mock directory:

```bash
cd backend/trms-mock
```

#### Understanding the Project Structure

The TRMS mock system follows standard Spring Boot conventions:

```
trms-mock/
├── src/main/java/com/example/trmsmock/
│   ├── model/              # JPA Entities
│   │   ├── Account.java
│   │   └── Transaction.java
│   ├── repository/         # Spring Data JPA Repositories
│   │   ├── AccountRepository.java
│   │   └── TransactionRepository.java
│   ├── service/           # Business Logic
│   │   ├── TrmsService.java
│   │   └── DataInitializationService.java
│   ├── controller/        # REST Controllers
│   │   └── TrmsController.java
│   └── TrmsMockApplication.java
├── src/main/resources/
│   └── application.properties
└── pom.xml
```

#### Build and Test the TRMS Mock System

1. **Verify Maven Dependencies**
   ```bash
   mvn clean compile
   ```
   This command will download all required dependencies and compile the project.

2. **Run Tests (if any)**
   ```bash
   mvn test
   ```

3. **Start the Application**
   ```bash
   mvn spring-boot:run
   ```

4. **Verify the Service is Running**
   Open a new terminal and test the health endpoint:
   ```bash
   curl http://localhost:8080/api/trms/health
   ```
   Expected response:
   ```json
   {
     "status": "healthy",
     "service": "TRMS Mock System",
     "version": "1.0.0"
   }
   ```

5. **Test Sample Data**
   ```bash
   curl http://localhost:8080/api/trms/accounts
   ```
   This should return a list of pre-loaded sample accounts.

### Setting Up the AI Integration Service

Open a new terminal and navigate to the AI service directory:

```bash
cd backend/trms-ai
```

#### Understanding the AI Service Structure

```
trms-ai/
├── src/main/java/com/example/trmsai/
│   ├── model/             # Data Models
│   │   ├── ChatRequest.java
│   │   └── ChatResponse.java
│   ├── service/           # AI Logic
│   │   ├── TrmsAiService.java
│   │   └── TrmsFunctions.java
│   ├── controller/        # REST Controllers
│   │   └── ChatController.java
│   ├── config/           # Configuration
│   │   └── AiConfiguration.java
│   └── TrmsAiApplication.java
├── src/main/resources/
│   └── application.properties
└── pom.xml
```

#### Build and Test the AI Service

1. **Compile the Project**
   ```bash
   mvn clean compile
   ```

2. **Start the AI Service**
   ```bash
   mvn spring-boot:run
   ```

3. **Verify the Service is Running**
   ```bash
   curl http://localhost:8081/api/chat/health
   ```
   Expected response:
   ```json
   {
     "status": "healthy",
     "service": "TRMS AI Chat Service",
     "version": "1.0.0",
     "trms_connection": "mocked"
   }
   ```

4. **Test Chat Functionality**
   ```bash
   curl -X POST http://localhost:8081/api/chat \
     -H "Content-Type: application/json" \
     -d '{"message": "Show me USD accounts"}'
   ```

## Frontend Setup

### Setting Up the React Application

Open a new terminal and navigate to the frontend directory:

```bash
cd frontend/trms-chat
```

#### Understanding the Frontend Structure

```
trms-chat/
├── public/                # Static assets
├── src/
│   ├── assets/           # Images and static files
│   ├── components/       # React components
│   │   └── ui/          # shadcn/ui components
│   ├── App.css          # Application styles
│   ├── App.jsx          # Main application component
│   ├── index.css        # Global styles
│   └── main.jsx         # Application entry point
├── components.json       # shadcn/ui configuration
├── index.html           # HTML template
├── package.json         # Dependencies and scripts
├── pnpm-lock.yaml       # Lock file
└── vite.config.js       # Vite configuration
```

#### Install Dependencies and Start Development Server

1. **Install Dependencies**
   ```bash
   pnpm install
   ```
   This will install all required npm packages including React, Vite, Tailwind CSS, and shadcn/ui components.

2. **Start the Development Server**
   ```bash
   pnpm run dev --host
   ```
   The `--host` flag allows external access to the development server.

3. **Verify the Frontend is Running**
   Open your web browser and navigate to `http://localhost:5173`
   You should see the TRMS AI Assistant landing page with a centered input field.

## Running the Application

### Option 1: Using the Startup Script (Recommended)

The project includes a convenient startup script that launches all services in the correct order:

```bash
chmod +x start-all.sh
./start-all.sh
```

This script will:
1. Check if required ports are available
2. Start the TRMS mock system on port 8080
3. Start the AI service on port 8081
4. Start the React frontend on port 5173
5. Display service URLs and status

### Option 2: Manual Startup

If you prefer to start services manually or need to troubleshoot:

#### Terminal 1: TRMS Mock System
```bash
cd backend/trms-mock
mvn spring-boot:run
```

#### Terminal 2: AI Service
```bash
cd backend/trms-ai
mvn spring-boot:run
```

#### Terminal 3: React Frontend
```bash
cd frontend/trms-chat
pnpm run dev --host
```

### Verifying All Services

Once all services are running, verify they're working correctly:

1. **TRMS Mock System**: http://localhost:8080/api/trms/health
2. **AI Service**: http://localhost:8081/api/chat/health
3. **React Frontend**: http://localhost:5173

## Testing the System

### Basic Functionality Tests

1. **Open the Application**
   Navigate to http://localhost:5173 in your web browser.

2. **Test the Landing Page**
   - You should see a clean, Google-like interface
   - There should be a centered input field with placeholder text
   - The page should be responsive on different screen sizes

3. **Test Chat Functionality**
   Type any message and press Enter. The interface should:
   - Animate the input field to the bottom
   - Transform into a chat interface
   - Display your message and an AI response

### Sample Test Queries

Try these queries to test different functionalities:

#### Account Queries
- "Show me all USD accounts"
- "List EUR accounts"
- "What accounts do we have?"

#### Balance Checks
- "Check balance for ACC-001-USD"
- "What's the balance of ACC-003-EUR?"
- "Show balance for ACC-005-GBP"

#### Transaction Queries
- "Show recent transactions"
- "List all transactions"
- "Transaction history"

#### Help and Information
- "What can you help me with?"
- "Help"
- "What features are available?"

### Expected Responses

The AI service should provide detailed, formatted responses for each query type. For example:

- Account queries return lists of accounts with IDs and statuses
- Balance checks show current balance with currency
- Transaction queries display recent transaction history
- Help queries explain available features

## Troubleshooting

### Common Issues and Solutions

#### Port Already in Use

**Problem**: Error message indicating port 8080, 8081, or 5173 is already in use.

**Solution**:
```bash
# Check what's using the ports
lsof -i :8080
lsof -i :8081
lsof -i :5173

# Kill the processes if needed
kill <PID>
```

#### Java Version Issues

**Problem**: Maven or Spring Boot fails to start due to Java version.

**Solution**:
1. Verify Java version: `java -version`
2. Ensure JAVA_HOME is set correctly
3. Update to Java 17 or higher if needed

#### Maven Dependencies Not Downloading

**Problem**: Maven fails to download dependencies.

**Solution**:
```bash
# Clear Maven cache and retry
mvn clean
mvn dependency:purge-local-repository
mvn clean compile
```

#### React Dependencies Issues

**Problem**: Frontend fails to start due to dependency issues.

**Solution**:
```bash
# Clear node_modules and reinstall
rm -rf node_modules pnpm-lock.yaml
pnpm install
```

#### API Connection Issues

**Problem**: Frontend cannot connect to backend services.

**Solution**:
1. Verify backend services are running
2. Check Vite proxy configuration in `vite.config.js`
3. Ensure CORS is properly configured in Spring Boot

#### H2 Database Issues

**Problem**: TRMS mock system fails to start due to database errors.

**Solution**:
1. Check if H2 database files are corrupted
2. Restart the application (H2 is in-memory, so it will recreate)
3. Verify application.properties configuration

### Debugging Tips

#### Enable Debug Logging

Add these properties to `application.properties` for more detailed logging:

```properties
# For TRMS Mock System
logging.level.com.example.trmsmock=DEBUG
logging.level.org.springframework.web=DEBUG

# For AI Service
logging.level.com.example.trmsai=DEBUG
logging.level.org.springframework.ai=DEBUG
```

#### Browser Developer Tools

Use browser developer tools to debug frontend issues:
1. Open Developer Tools (F12)
2. Check Console tab for JavaScript errors
3. Check Network tab for API call failures
4. Use React Developer Tools extension for component debugging

#### API Testing with curl

Test backend APIs directly:

```bash
# Test TRMS endpoints
curl http://localhost:8080/api/trms/accounts
curl http://localhost:8080/api/trms/accounts/ACC-001-USD/balance

# Test AI endpoints
curl -X POST http://localhost:8081/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "test message"}'
```

## Development Workflow

### Making Changes to the Backend

1. **Modify Java Code**
   - Edit files in `src/main/java/`
   - Spring Boot DevTools will automatically restart the application

2. **Update Configuration**
   - Modify `application.properties`
   - Restart the application manually

3. **Add New Dependencies**
   - Update `pom.xml`
   - Run `mvn clean compile` to download new dependencies

### Making Changes to the Frontend

1. **Modify React Components**
   - Edit files in `src/`
   - Vite will automatically reload the browser

2. **Add New Dependencies**
   - Run `pnpm add <package-name>`
   - Update imports in your components

3. **Update Styling**
   - Modify CSS files or Tailwind classes
   - Changes are reflected immediately

### Testing Changes

1. **Backend Testing**
   ```bash
   mvn test
   curl http://localhost:8080/api/trms/health
   ```

2. **Frontend Testing**
   - Use browser developer tools
   - Test user interactions manually
   - Verify responsive design on different screen sizes

### Building for Production

#### Backend Build
```bash
cd backend/trms-mock
mvn clean package

cd ../trms-ai
mvn clean package
```

#### Frontend Build
```bash
cd frontend/trms-chat
pnpm run build
```

The built files will be in the `dist/` directory and can be served by any static file server.

## Next Steps

After successfully setting up the application, you can:

1. **Integrate Real AI**: Replace mocked AI responses with actual Spring AI and Ollama integration
2. **Add Authentication**: Implement user authentication and authorization
3. **Enhance UI**: Add more sophisticated charts and dashboards
4. **Add Testing**: Implement unit tests and integration tests
5. **Deploy to Production**: Use Docker containers and cloud deployment

This setup guide provides a solid foundation for developing and extending the TRMS AI Assistant. The modular architecture makes it easy to enhance individual components without affecting others.

