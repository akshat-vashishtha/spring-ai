# Spring AI Learning & Showcase Project 🚀

A comprehensive Spring Boot application showcasing **Spring AI** capabilities including Conversational Chat Memory, Basic & Advanced RAG, Function / Tool Calling, Multi-format Document ETL Pipeline, Vector Store Search (Qdrant), Speech-to-Text Transcription, and **Model Context Protocol (MCP)** — both as a server exposing tools and as a client connecting to external MCP servers.

---

## 📑 Table of Contents
- [Architecture & Key Features](#-architecture--key-features)
- [Prerequisites](#-prerequisites)
- [Infrastructure Setup (Docker)](#-infrastructure-setup-docker)
- [Configuration & API Keys](#-configuration--api-keys)
- [Building & Running the Application](#-building--running-the-application)
- [Using Swagger UI (OpenAPI)](#-using-swagger-ui-openapi)
- [API Endpoints & Testing Guide](#-api-endpoints--testing-guide)
  - [1. Chat API](#1-chat-api)
  - [2. Tool Calling API](#2-tool-calling-api)
  - [3. Knowledge Base API](#3-knowledge-base-api)
  - [4. ETL Pipeline API](#4-etl-pipeline-api)
  - [5. Audio Transcription API](#5-audio-transcription-api)
  - [6. Internal MCP — Product Catalog](#6-internal-mcp--product-catalog-server--client)
  - [7. External MCP — Draw.io](#7-external-mcp--drawio-client)
- [Project Structure](#-project-structure)
- [MCP Architecture Deep Dive](#-mcp-architecture-deep-dive)
- [Troubleshooting](#-troubleshooting)

---

## 🌟 Architecture & Key Features

```
                                  +-----------------------------+
                                  |     OpenAI API Platform     |
                                  | (gpt-4o-mini / whisper-1)   |
                                  +--------------^--------------+
                                                 |
+---------------------+           +--------------v--------------+           +----------------------+
|   Swagger UI /      |  HTTP     |    Spring Boot (Spring AI)  |   gRPC    |    Qdrant Vector     |
|   REST Clients      +---------->| - ChatClient & Advisors     +---------->|    Database          |
+---------------------+           | - ETL Pipeline & Splitters  |  (6334)   | (Embeddings: 1536d)  |
                                  | - Function / Tool Calling   |           +----------------------+
                                  | - Whisper Audio Service     |
                                  | - MCP Server (product-catalog)|               (27017)
                                  | - MCP Client (drawio)       +---------->  MongoDB Database
                                  +--------------^--------------+           (Conversational Memory
                                                 | stdio                     + Product Catalog)
                                  +--------------v--------------+
                                  |   Draw.io MCP Server        |
                                  |   (@drawio/mcp via npx)     |
                                  +-----------------------------+
```

### Key Features
1. **Conversational Memory**: Chat history stored persistently in MongoDB per `conversationId`.
2. **Retrieval-Augmented Generation (RAG)**:
   - **Basic RAG**: Vector search with Question-Answer Advisor.
   - **Advanced RAG**: Contextual query augmentation and re-ranking for higher relevance.
3. **Dynamic Prompting**: Parameterized prompt generation (topic, persona, tone, language, format).
4. **Tool / Function Calling**:
   - **Local Tools**: DateTime utilities and mathematical expressions.
   - **External REST Tools**: Live JSONPlaceholder Users API.
   - **Combined Multi-Tool Calling**: Model autonomously chains multiple tools.
5. **Multi-Format Document ETL**: Ingest and chunk PDF, JSON, DOCX, Markdown, Text, and HTML into Qdrant vector store.
6. **Audio Transcription**: Speech-to-text powered by OpenAI Whisper.
7. **Model Context Protocol (MCP)**:
   - **Internal MCP Server** (`product-catalog`): Exposes Product CRUD tools over MCP (WebMVC/SSE).
   - **External MCP Client** (`drawio`): Connects to the official Draw.io MCP Server via stdio to generate diagrams.
8. **Interactive OpenAPI / Swagger Documentation**: Full interactive Swagger UI out of the box.

---

## 📋 Prerequisites

Ensure you have the following installed on your machine:
- **Java 21** or later (`java -version`)
- **Maven 3.9+** (`mvn -version`)
- **Docker** / **Docker Compose** (for MongoDB and Qdrant)
- **Node.js 18+** with `npx` — required for the Draw.io MCP Server (`npx -y @drawio/mcp`)
- **OpenAI API Key** ([platform.openai.com](https://platform.openai.com/))

---

## 🐳 Infrastructure Setup (Docker)

Start MongoDB and Qdrant vector database using Docker:

### Option 1: Run individual Docker containers

```bash
# 1. Start MongoDB (Port 27017)
docker run -d \
  --name mongodb \
  -p 27017:27017 \
  mongo:latest

# 2. Start Qdrant Vector Database (HTTP: 6333, gRPC: 6334)
docker run -d \
  --name qdrant \
  -p 6333:6333 \
  -p 6334:6334 \
  -v $(pwd)/qdrant_data:/qdrant/storage \
  qdrant/qdrant:latest
```

### Option 2: Verify running containers
```bash
docker ps
```
You should see `mongodb` (port 27017) and `qdrant` (ports 6333, 6334) in running state.

---

## ⚙️ Configuration & API Keys

Configure your OpenAI API key in `src/main/resources/application.yaml` or set it as an environment variable:

### Using Environment Variable (Recommended)
```bash
export SPRING_AI_OPENAI_API_KEY="sk-proj-your-actual-api-key"
```

### Or update `src/main/resources/application.yaml` directly:
```yaml
spring:
  ai:
    openai:
      api-key: Place your Open API Key here
      chat:
        options:
          model: gpt-4o-mini
      embedding:
        options:
          model: text-embedding-3-small
      audio:
        transcription:
          model: whisper-1
    mcp:
      server:
        enabled: true
        name: product-catalog   # Internal MCP Server name
        version: 1.0.0
        type: SYNC
      client:
        enabled: true
        type: SYNC
        annotation-scanner:
          enabled: false
        stdio:
          connections:
            drawio:             # External Draw.io MCP Server
              command: npx
              args:
                - "-y"
                - "@drawio/mcp"
```

---

## 🚀 Building & Running the Application

### 1. Build the project
```bash
mvn clean compile -DskipTests
```

### 2. Run the Spring Boot application
```bash
mvn spring-boot:run
```

Once the application starts, it will listen on `http://localhost:8080`.

On startup you should see in the logs:
```
McpServerAutoConfiguration : Registered tools: 5          ← Internal Product MCP Server UP
StdioClientTransport       : Draw.io MCP server running on stdio  ← External Draw.io MCP Client connected
SpringAiApplication        : Started SpringAiApplication in ~5s
```

---

## 📖 Using Swagger UI (OpenAPI)

Once the application is running, open your web browser and navigate to:

👉 **[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)**
*(or [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html))*

### Raw OpenAPI JSON Specification:
👉 **[http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)**

### How to test endpoints in Swagger UI:
1. Open the Swagger UI page in your browser.
2. Click on any section (e.g., **Chat API**, **Internal Product MCP API**, **External Draw.io MCP API**).
3. Expand any endpoint and click **"Try it out"**.
4. Fill in the query parameters or request body.
5. Click **"Execute"** to inspect the live response, status code, and headers!

---

## 📡 API Endpoints & Testing Guide

### 1. Chat API

#### 💬 Standard Chat with Memory
```bash
curl -X GET "http://localhost:8080/api/chat/ask?prompt=My+name+is+Akshat&conversationId=session-1"
curl -X GET "http://localhost:8080/api/chat/ask?prompt=What+is+my+name?&conversationId=session-1"
```

#### 📚 RAG (Knowledge Retrieval)
```bash
curl -X GET "http://localhost:8080/api/chat/rag?prompt=What+is+Spring+AI?&conversationId=session-1"
```

#### 🔍 Advanced RAG (Contextual Query Augmentation)
```bash
curl -X GET "http://localhost:8080/api/chat/rag/advance?prompt=Explain+architecture+patterns&conversationId=session-1"
```

#### 🎯 Dynamic Prompt Template
```bash
curl -X POST "http://localhost:8080/api/chat/dynamic?conversationId=session-1" \
  -H "Content-Type: application/json" \
  -d '{
    "topic": "Microservices vs Monoliths",
    "persona": "Chief Architect",
    "tone": "concise and analytical",
    "language": "English",
    "format": "bullet points with pros and cons",
    "additionalInstructions": "Highlight database scalability considerations"
  }'
```

#### ⚡ Real-Time Streaming (SSE)
```bash
curl -N -X GET "http://localhost:8080/api/chat/streaming?prompt=Write+a+poem+about+coding&conversationId=stream-1"
```

---

### 2. Tool Calling API

#### 🧮 Local Tools (DateTime & Calculator)
```bash
curl -X GET "http://localhost:8080/api/chat/tools/normal?prompt=What+is+the+current+UTC+time+and+calculate+15+percent+of+850?&conversationId=tool-1"
```

#### 🌐 External REST API Tools (JSONPlaceholder Users)
```bash
curl -X GET "http://localhost:8080/api/chat/tools/users?prompt=Give+me+details+for+user+with+id+1&conversationId=tool-1"
```

#### 🛠️ All Tools Combined
```bash
curl -X GET "http://localhost:8080/api/chat/tools/all?prompt=Who+is+user+2+and+convert+their+temperature+25+Celsius+to+Fahrenheit?&conversationId=tool-1"
```

---

### 3. Knowledge Base API

#### 📥 Add Document to Vector Store
```bash
curl -X POST "http://localhost:8080/api/knowledge/documents" \
  -H "Content-Type: application/json" \
  -d '{
    "content": "Spring AI provides high-level abstractions for AI models and vector stores in Spring Boot.",
    "metadata": {
      "category": "framework",
      "author": "Spring Team"
    }
  }'
```

#### 🔎 Semantic Vector Search
```bash
curl -X GET "http://localhost:8080/api/knowledge/search?query=Spring+AI+framework+abstractions&topK=3&similarityThreshold=0.6"
```

---

### 4. ETL Pipeline API

#### 📁 Batch Ingest Local Folder into Qdrant
```bash
curl -X POST "http://localhost:8080/api/etl/ingest-folder?folderPath=src/main/resources/documents"
```
*Supported formats: `.pdf`, `.json`, `.docx`, `.md`, `.txt`, `.html`.*

---

### 5. Audio Transcription API

#### 🎙️ Transcribe Sample Audio
```bash
curl -X GET "http://localhost:8080/api/audio/transcription/sample"
```

#### 📤 Transcribe Custom Audio File
```bash
curl -X POST "http://localhost:8080/api/audio/transcription/upload/custom" \
  -F "file=@/path/to/audio.mp3" \
  -F "prompt=Technical discussion on Spring AI" \
  -F "language=en"
```

---

### 6. Internal MCP — Product Catalog (Server + Client)

This app runs an **internal MCP Server** (`product-catalog`) exposing Product CRUD tools. The AI client calls these tools via natural language prompts.

**Available MCP Tools registered on the server:**
| Tool Name | Description |
|---|---|
| `addProduct` | Add a new product to the MongoDB catalog |
| `getProduct` | Get a product by ID |
| `getAllProducts` | List all products in the catalog |
| `updateProduct` | Update product fields by ID |
| `deleteProduct` | Delete a product by ID |

#### 💬 Chat with Internal Product MCP Tools
```bash
# Add a product
curl -X GET "http://localhost:8080/api/mcp/internal/products/chat?prompt=Add+a+MacBook+Pro+M4+for+2499.99+USD+in+Electronics+with+50+stock&conversationId=product-1"

# Get all products
curl -X GET "http://localhost:8080/api/mcp/internal/products/chat?prompt=List+all+products+in+the+catalog&conversationId=product-1"

# Update a product
curl -X GET "http://localhost:8080/api/mcp/internal/products/chat?prompt=Update+the+price+of+product+ID+<id>+to+2299.99&conversationId=product-1"

# Delete a product
curl -X GET "http://localhost:8080/api/mcp/internal/products/chat?prompt=Delete+product+with+id+<id>&conversationId=product-1"
```

---

### 7. External MCP — Draw.io Client

This app connects as an **MCP Client** to the official **Draw.io MCP Server** (`@drawio/mcp`) via stdio. The AI model can generate and open Draw.io diagrams using natural language.

#### 🎨 Generate a Draw.io Diagram
```bash
# Simple architecture diagram
curl -X GET "http://localhost:8080/api/mcp/external/drawio/generate?prompt=simple+client-API-server-database+diagram&conversationId=drawio-1"

# AWS microservices architecture
curl -X GET "http://localhost:8080/api/mcp/external/drawio/generate?prompt=AWS+microservices+architecture+with+API+Gateway+Lambda+DynamoDB+and+S3&conversationId=drawio-2"

# CI/CD pipeline flow
curl -X GET "http://localhost:8080/api/mcp/external/drawio/generate?prompt=CI+CD+pipeline+from+GitHub+to+Docker+to+Kubernetes&conversationId=drawio-3"
```

The response includes a **clickable Draw.io link** that opens your generated diagram directly in the browser!

#### 🔧 List All Draw.io MCP Tools
```bash
curl -X GET "http://localhost:8080/api/mcp/external/drawio/tools"
```

---

## 📁 Project Structure

```
spring-ai/
├── pom.xml
├── README.md
└── src/
    └── main/
        ├── java/com/learning/
        │   ├── SpringAiApplication.java
        │   ├── advisor/                     # Custom Execution Audit Advisor (token tracking)
        │   ├── audiotranscription/
        │   │   ├── controller/              # Audio Transcription REST Controller
        │   │   └── service/                 # OpenAI Whisper Integration Service
        │   ├── config/                      # OpenAPI, AI, RAG & Properties Config
        │   ├── controller/                  # Chat, Knowledge & Tool REST Controllers
        │   ├── dto/                         # API Request & Response DTOs
        │   ├── etl/                         # Document ETL Pipeline
        │   │   ├── config/                  # ETL configuration
        │   │   ├── controller/              # ETL REST Controller
        │   │   ├── loader/                  # VectorStore Document Loader
        │   │   ├── model/                   # DocumentType enum
        │   │   ├── reader/                  # PDF, JSON, HTML Document Readers
        │   │   ├── service/                 # ETL Orchestration Service
        │   │   └── transformer/             # Document Chunk Transformer
        │   ├── mapper/                      # ChatResponse & Document Mappers
        │   ├── mcp/
        │   │   ├── internal/                # 🔵 Internal MCP (Server + Client in-process)
        │   │   │   ├── client/
        │   │   │   │   ├── ProductMcpChatService.java   # AI ChatClient using ProductMcpTools
        │   │   │   │   └── ProductMcpController.java    # REST → AI → MCP Tool call
        │   │   │   ├── model/
        │   │   │   │   └── Product.java                 # MongoDB Document model
        │   │   │   ├── repository/
        │   │   │   │   └── ProductRepository.java       # MongoDB Repository
        │   │   │   ├── server/
        │   │   │   │   └── ProductMcpTools.java         # @McpTool annotated tools (CRUD)
        │   │   │   └── service/
        │   │   │       └── ProductService.java          # Product business logic
        │   │   └── external/                # 🟢 External MCP Client (Draw.io)
        │   │       ├── DrawioMcpChatService.java        # AI + SyncMcpToolCallbackProvider
        │   │       └── DrawioMcpController.java         # REST → AI → Draw.io MCP tool call
        │   ├── prompt/                      # Dynamic Prompt Templates & Factory
        │   ├── service/                     # Chat, Streaming & Knowledge Services
        │   ├── tools/                       # DateTime, Calculator & External API Tools
        │   └── validation/                  # SafeGuard & Request Input Validators
        └── resources/
            ├── application.yaml             # Full Application & MCP Configuration
            └── documents/                   # Sample ETL documents (PDF, JSON, etc.)
```

---

## 🧩 MCP Architecture Deep Dive

### What is MCP (Model Context Protocol)?
MCP is an open protocol that allows AI models to call external tools, access resources, and execute actions in a standardized way — similar to how REST APIs work but specifically designed for AI tool calling.

### How This Project Uses MCP

```
┌─────────────────────────────────────────────────────────────────────┐
│                       Spring Boot Application                        │
│                                                                     │
│  ┌───────────────────────────────────────────┐                     │
│  │         INTERNAL MCP (in-process)          │                     │
│  │                                           │                     │
│  │  ProductMcpController                     │                     │
│  │       │                                   │                     │
│  │       ▼                                   │                     │
│  │  ProductMcpChatService ──► ChatClient     │                     │
│  │       │                         │         │                     │
│  │       │                         ▼         │                     │
│  │       │                    OpenAI API      │                     │
│  │       │                         │         │                     │
│  │       │              (tool call decided)   │                     │
│  │       │                         ▼         │                     │
│  │       └────────────► ProductMcpTools      │                     │
│  │                      (addProduct,          │                     │
│  │                       getProduct, etc.)    │                     │
│  │                            │               │                     │
│  │                            ▼               │                     │
│  │                        MongoDB             │                     │
│  └───────────────────────────────────────────┘                     │
│                                                                     │
│  ┌───────────────────────────────────────────┐                     │
│  │   EXTERNAL MCP CLIENT (Draw.io via stdio)  │                     │
│  │                                           │                     │
│  │  DrawioMcpController                      │                     │
│  │       │                                   │                     │
│  │       ▼                                   │                     │
│  │  DrawioMcpChatService ──► ChatClient      │                     │
│  │                   SyncMcpToolCallbackProvider                   │
│  │                               │           │                     │
│  │                               ▼           │                     │
│  │                          OpenAI API       │                     │
│  │                               │           │                     │
│  │                    (tool call decided)    │                     │
│  │                               │           │                     │
│  │                    stdio transport        │                     │
│  │                               ▼           │                     │
│  │                   @drawio/mcp (npx)       │                     │
│  │                  (external process)       │                     │
│  └───────────────────────────────────────────┘                     │
└─────────────────────────────────────────────────────────────────────┘
```

### MCP Configuration Reference (`application.yaml`)

```yaml
spring:
  ai:
    mcp:
      server:
        enabled: true          # Enable the internal MCP Server
        name: product-catalog  # Server identity name (no spaces)
        version: 1.0.0         # Server version
        type: SYNC             # SYNC = synchronous (blocking) execution

      client:
        enabled: true          # Enable MCP client (connects to external servers)
        type: SYNC             # Must match server type
        annotation-scanner:
          enabled: false       # Disable classpath scanning (not needed here)
        stdio:
          connections:
            drawio:            # Connection alias (can be any unique name)
              command: npx     # Command to launch the external MCP server process
              args:
                - "-y"
                - "@drawio/mcp"
```

### Key Classes

| Class | Role |
|---|---|
| [`ProductMcpTools`](src/main/java/com/learning/mcp/internal/server/ProductMcpTools.java) | MCP Server — defines tools with `@McpTool` and `@Tool` annotations |
| [`ProductMcpChatService`](src/main/java/com/learning/mcp/internal/client/ProductMcpChatService.java) | Internal client — passes `productMcpTools` to `ChatClient.tools()` |
| [`DrawioMcpChatService`](src/main/java/com/learning/mcp/external/DrawioMcpChatService.java) | External client — passes `SyncMcpToolCallbackProvider` (auto-discovered Draw.io tools) |
| `SyncMcpToolCallbackProvider` | Spring AI bean that holds all tools discovered from connected external MCP servers |

---

## 🛠️ Troubleshooting

1. **MongoDB Connection Refused (`localhost:27017`)**:
   - Ensure MongoDB Docker container is running: `docker start mongodb` or `docker run -d -p 27017:27017 mongo:latest`.

2. **Qdrant gRPC Connection Error (`localhost:6334`)**:
   - Ensure Qdrant container is running: `docker start qdrant` or `docker run -d -p 6333:6333 -p 6334:6334 qdrant/qdrant:latest`.

3. **OpenAI 401 Unauthorized / Invalid API Key**:
   - Verify your API key is correctly specified in `application.yaml` or exported as `SPRING_AI_OPENAI_API_KEY`.

4. **Draw.io MCP Server not connecting (`npx: command not found`)**:
   - Install Node.js 18+ and verify: `node -v` and `npx -v`.
   - Try manually: `npx -y @drawio/mcp` to ensure the package installs correctly.

5. **App crashes on startup with `NoClassDefFoundError: TypeInformation`**:
   - Ensure `annotation-scanner.enabled: false` is set under `spring.ai.mcp.client` in `application.yaml`.

6. **Swagger UI Not Loading**:
   - Verify the server is running on `http://localhost:8080`.
   - Access via direct URL: `http://localhost:8080/swagger-ui/index.html`.
