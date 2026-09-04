# Spring AI Showcase & Reference Project 🚀

A Spring Boot application showcasing **Spring AI (2.0.1)** capabilities, including Persistent Chat Memory, Basic & Advanced RAG, Autonomous Function / Tool Calling, Multi-Format Document ETL Pipeline, Vector Store Search (Qdrant), Audio Transcription (OpenAI Whisper), AI Image Generation & Download, and **Model Context Protocol (MCP)** — both as an internal server exposing tools and as a client orchestrating external MCP servers.

---

## 📑 Table of Contents
- [🌟 Architecture & System Overview](#-architecture--system-overview)
- [✨ Key Features](#-key-features)
- [📋 Prerequisites](#-prerequisites)
- [🐳 Infrastructure Setup (Docker)](#-infrastructure-setup-docker)
- [⚙️ Configuration & Environment Variables](#️-configuration--environment-variables)
- [🚀 Building & Running the Application](#-building--running-the-application)
- [📖 Interactive Swagger UI (OpenAPI)](#-interactive-swagger-ui-openapi)
- [📡 API Endpoints & Testing Guide](#-api-endpoints--testing-guide)
  - [1. Chat & Conversational Memory API](#1-chat--conversational-memory-api)
  - [2. Function / Tool Calling API](#2-function--tool-calling-api)
  - [3. Knowledge Base & Vector Search API](#3-knowledge-base--vector-search-api)
  - [4. Document ETL Ingestion Pipeline API](#4-document-etl-ingestion-pipeline-api)
  - [5. Audio Transcription API (Whisper)](#5-audio-transcription-api-whisper)
  - [6. Image Generation & Download API](#6-image-generation--download-api)
  - [7. Internal MCP — Product Catalog (Server + Client)](#7-internal-mcp--product-catalog-server--client)
  - [8. External MCP — Draw.io Diagram Generator](#8-external-mcp--drawio-diagram-generator)
- [📁 Project Structure](#-project-structure)
- [🧩 Deep Dive: Model Context Protocol (MCP)](#-deep-dive-model-context-protocol-mcp)
- [🛡️ Governance & Advisors](#️-governance--advisors)
- [🛠️ Troubleshooting & Gotchas](#️-troubleshooting--gotchas)

---

## 🌟 Architecture & System Overview

```
                                  +------------------------------------+
                                  |        OpenAI Platform APIs        |
                                  | - Chat: gpt-4o-mini                |
                                  | - Embeddings: text-embedding-3     |
                                  | - Audio: whisper-1                 |
                                  | - Images: gpt-image-1-mini         |
                                  +-----------------^------------------+
                                                    |
+---------------------+              +--------------v------------------+              +----------------------+
|   Swagger UI /      |   HTTP/SSE   |     Spring Boot (Spring AI)     |     gRPC     |    Qdrant Vector     |
|   REST Clients      +------------->| - ChatClient & Custom Advisors  +------------->|    Database          |
+---------------------+              | - ETL Pipeline & Chunk Splitters|    (6334)    | (Embeddings: 1536d)  |
                                     | - Dynamic Multi-Tool Calling    |              +----------------------+
                                     | - Whisper Audio Speech-to-Text  |
                                     | - Image Generation & Download   |                  (27017)
                                     | - MCP Server (product-catalog)  +------------> MongoDB Database
                                     | - MCP Client (drawio via stdio) |              (Conversational Memory
                                     +--------------^------------------+               + Product Catalog)
                                                    | stdio
                                     +--------------v------------------+
                                     |   Draw.io External MCP Server   |
                                     |      (@drawio/mcp via npx)      |
                                     +---------------------------------+
```

---

## ✨ Key Features

1. **Persistent Conversational Memory**: Thread-safe chat history stored in MongoDB using `MessageChatMemoryAdvisor`, referenced by `conversationId`.
2. **Retrieval-Augmented Generation (RAG)**:
   - **Basic RAG**: Vector similarity search with `QuestionAnswerAdvisor`.
   - **Advanced Contextual RAG**: Dynamic query expansion and re-ranking for enhanced retrieval precision.
3. **Dynamic Prompt Templating**: Fine-grained template generation controlling topic, persona, tone, language, and output format.
4. **Autonomous Function / Tool Calling**:
   - **Local Tools**: DateTime utilities and mathematical expression evaluator.
   - **External REST API Tools**: Live JSONPlaceholder integration for user data retrieval.
   - **Dynamic Multi-Tool Orchestration**: Model automatically decides which tools to invoke and chains outputs.
5. **Multi-Format Document ETL Pipeline**: Automated document reader, chunk transformer, and loader supporting **PDF**, **JSON**, **DOCX**, **Markdown**, **Plain Text**, and **HTML** into Qdrant.
6. **Audio Transcription**: High-fidelity speech-to-text conversion powered by OpenAI Whisper (`whisper-1`).
7. **Direct Image Generation & Download**: AI image creation powered by OpenAI image models (`gpt-image-1-mini` / `gpt-image-1`) streaming binary `image/png` bytes directly for in-browser rendering (`inline`) and file downloads (`attachment`).
8. **Model Context Protocol (MCP)**:
   - **Internal MCP Server** (`product-catalog`): In-process WebMVC MCP server exposing MongoDB Product CRUD tools.
   - **External MCP Client** (`drawio`): Stdio transport connecting to `@drawio/mcp` to autonomously generate live, editable diagrams.
9. **Enterprise Governance & Observability**:
   - `SafeGuardAdvisor`: Keyword-based content moderation guardrail.
   - `ExecutionAuditAdvisor`: Custom audit interceptor tracking token usage and execution latency.
10. **Interactive Swagger UI**: Out-of-the-box OpenAPI 3.0 documentation with interactive "Try It Out" support.

---

## 📋 Prerequisites

- **Java 21** or later (`java -version`)
- **Maven 3.9+** (`mvn -version`)
- **Docker** & **Docker Compose** (for MongoDB and Qdrant)
- **Node.js 18+** with `npx` (required for Draw.io MCP Server: `npx -y @drawio/mcp`)
- **OpenAI API Key** ([platform.openai.com](https://platform.openai.com/))

---

## 🐳 Infrastructure Setup (Docker)

Start the required backing services (MongoDB and Qdrant):

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

Verify containers are running:
```bash
docker ps
```

---

## ⚙️ Configuration & Environment Variables

Configure your credentials in `src/main/resources/application.yaml` or set environment variables:

```bash
export SPRING_AI_OPENAI_API_KEY="sk-proj-your-api-key-here"
```

### Key Configuration Reference (`src/main/resources/application.yaml`)

```yaml
spring:
  application:
    name: spring-ai
  mongodb:
    uri: mongodb://localhost:27017/spring-ai
  ai:
    chat:
      memory:
        repository:
          mongo:
            create-indices: true
    openai:
      api-key: ${SPRING_AI_OPENAI_API_KEY}
      chat:
        options:
          model: gpt-4o-mini
      embedding:
        options:
          model: text-embedding-3-small
      audio:
        transcription:
          model: whisper-1
      image:
        options:
          model: gpt-image-1-mini
    vectorstore:
      qdrant:
        host: localhost
        port: 6334
        collection-name: spring_ai_documents
        use-tls: false
        initialize-schema: false
    mcp:
      server:
        enabled: true
        name: product-catalog
        version: 1.0.0
        type: SYNC
      client:
        enabled: true
        type: SYNC
        annotation-scanner:
          enabled: false
        stdio:
          connections:
            drawio:
              command: npx
              args:
                - "-y"
                - "@drawio/mcp"
```

---

## 🚀 Building & Running the Application

### 1. Build the Application
```bash
mvn clean compile -DskipTests
```

### 2. Run the Spring Boot App
```bash
mvn spring-boot:run
```

Once started, the application listens on `http://localhost:8080`.

**Startup Verification Logs:**
```
McpServerAutoConfiguration : Registered tools: 5          ← Product MCP Server Ready
StdioClientTransport       : Draw.io MCP server running on stdio  ← External Draw.io MCP Connected
SpringAiApplication        : Started SpringAiApplication in ~3.5s
```

---

## 📖 Interactive Swagger UI (OpenAPI)

Once running, access the interactive API docs directly in your browser:

👉 **[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)**  
*(Alternative URL: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html))*

**Raw OpenAPI JSON Specification:**  
👉 **[http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)**

---

## 📡 API Endpoints & Testing Guide

### 1. Chat & Conversational Memory API

#### 💬 Standard Chat with Conversation Memory
Remembers past context across multiple turns using `conversationId`:
```bash
# Turn 1
curl -X GET "http://localhost:8080/api/chat/ask?prompt=My+name+is+Alex&conversationId=session-101"

# Turn 2 (Verifies memory recall)
curl -X GET "http://localhost:8080/api/chat/ask?prompt=What+is+my+name?&conversationId=session-101"
```

#### 📚 Basic RAG (Vector Search)
Retrieves relevant indexed document chunks from Qdrant and injects them into the prompt context:
```bash
curl -X GET "http://localhost:8080/api/chat/rag?prompt=What+is+Spring+AI?&conversationId=session-101"
```

#### 🔍 Advanced RAG (Contextual Augmentation)
Performs query rewriting and re-ranking for improved recall:
```bash
curl -X GET "http://localhost:8080/api/chat/rag/advance?prompt=Explain+distributed+caching+patterns&conversationId=session-101"
```

#### 🎯 Dynamic Prompt Templating
```bash
curl -X POST "http://localhost:8080/api/chat/dynamic?conversationId=session-101" \
  -H "Content-Type: application/json" \
  -d '{
    "topic": "Microservices vs Monoliths",
    "persona": "Chief Architect",
    "tone": "concise and analytical",
    "language": "English",
    "format": "bullet points with trade-offs",
    "additionalInstructions": "Highlight operational complexity"
  }'
```

#### ⚡ Real-Time Streaming (SSE)
Streams tokens as Server-Sent Events (SSE):
```bash
curl -N -X GET "http://localhost:8080/api/chat/streaming?prompt=Tell+me+a+short+story+about+a+coder&conversationId=stream-1"
```

---

### 2. Function / Tool Calling API

#### 🧮 Local Tools (DateTime & Expression Calculator)
```bash
curl -X GET "http://localhost:8080/api/chat/tools/normal?prompt=What+is+the+current+UTC+time+and+calculate+15+percent+of+850?&conversationId=tool-1"
```

#### 🌐 External REST API Tool (JSONPlaceholder Users)
```bash
curl -X GET "http://localhost:8080/api/chat/tools/users?prompt=Fetch+details+for+user+id+1&conversationId=tool-1"
```

#### 🛠️ Combined Multi-Tool Calling
```bash
curl -X GET "http://localhost:8080/api/chat/tools/all?prompt=Who+is+user+2+and+convert+25+Celsius+to+Fahrenheit?&conversationId=tool-1"
```

---

### 3. Knowledge Base & Vector Search API

#### 📥 Add Document to Vector Store
```bash
curl -X POST "http://localhost:8080/api/knowledge/documents" \
  -H "Content-Type: application/json" \
  -d '{
    "content": "Spring AI provides high-level abstractions for AI models and vector databases.",
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

### 4. Document ETL Ingestion Pipeline API

Batch ingests and chunks documents from a folder into Qdrant. Supports `.pdf`, `.json`, `.docx`, `.md`, `.txt`, and `.html`:
```bash
curl -X POST "http://localhost:8080/api/etl/ingest-folder?folderPath=src/main/resources/documents"
```

---

### 5. Audio Transcription API (Whisper)

#### 🎙️ Transcribe Sample Audio
```bash
curl -X GET "http://localhost:8080/api/audio/transcription/sample"
```

#### 📤 Transcribe Custom Audio File
```bash
curl -X POST "http://localhost:8080/api/audio/transcription/upload/custom" \
  -F "file=@/path/to/meeting.mp3" \
  -F "prompt=Engineering team standup" \
  -F "language=en"
```

---

### 6. Image Generation & Download API

Generate images via OpenAI (`gpt-image-1-mini` / `gpt-image-1`) and directly stream PNG bytes without saving temporary files to disk.

#### 📥 Direct Download (`Content-Disposition: attachment`)
Triggers an immediate file download in your browser or writes directly to file via `curl`:
```bash
# Via GET query parameter:
curl "http://localhost:8080/api/image/download?prompt=A+serene+mountain+lake+at+sunset" -o lake.png

# Via POST JSON request body:
curl -X POST "http://localhost:8080/api/image/download" \
  -H "Content-Type: application/json" \
  -d '{"prompt": "A futuristic cyber city in neon lights"}' \
  -o cyber_city.png
```

#### 🖼️ Direct Browser View (`Content-Disposition: inline`)
Paste this URL directly into your browser to view the generated image rendered immediately on screen:
```
http://localhost:8080/api/image/generate?prompt=A+cute+golden+retriever+puppy
```
Or via POST:
```bash
curl -X POST "http://localhost:8080/api/image/generate" \
  -H "Content-Type: application/json" \
  -d '{"prompt": "A cute golden retriever puppy"}' \
  -o puppy.png
```

---

### 7. Internal MCP — Product Catalog (Server + Client)

An in-process **MCP Server** (`product-catalog`) exposes 5 CRUD tools to the AI model:
- `addProduct`: Creates a new product in MongoDB.
- `getProduct`: Finds a product by ID.
- `getAllProducts`: Lists all catalog items.
- `updateProduct`: Updates product name, price, stock, or category.
- `deleteProduct`: Removes a product by ID.

```bash
# Add a product via conversational prompt
curl -X GET "http://localhost:8080/api/mcp/internal/products/chat?prompt=Add+a+MacBook+Pro+M4+for+2499.99+USD+in+Electronics+with+50+stock&conversationId=prod-1"

# List all products
curl -X GET "http://localhost:8080/api/mcp/internal/products/chat?prompt=List+all+products+in+the+catalog&conversationId=prod-1"

# Update a product
curl -X GET "http://localhost:8080/api/mcp/internal/products/chat?prompt=Update+the+price+of+product+ID+<id>+to+2199.99&conversationId=prod-1"

# Delete a product
curl -X GET "http://localhost:8080/api/mcp/internal/products/chat?prompt=Delete+product+with+id+<id>&conversationId=prod-1"
```

---

### 8. External MCP — Draw.io Diagram Generator

Connects to the official **Draw.io MCP Server** (`@drawio/mcp`) running via stdio subprocess. The model generates valid diagram XML and returns a **clickable browser link** that opens the editable diagram directly in Draw.io.

```bash
# Generate architecture diagram
curl -X GET "http://localhost:8080/api/mcp/external/drawio/generate?prompt=Client+API-Gateway+Microservice+PostgreSQL+diagram&conversationId=draw-1"

# List all discovered Draw.io MCP tools
curl -X GET "http://localhost:8080/api/mcp/external/drawio/tools"
```

---

## 📁 Project Structure

```
spring-ai/
├── pom.xml                                  # Dependencies (Spring Boot 4.0.8, Spring AI 2.0.1, Qdrant, Mongo)
├── README.md                                # Comprehensive Project Documentation
└── src/
    └── main/
        ├── java/com/learning/
        │   ├── SpringAiApplication.java     # Application Boot Entrypoint
        │   ├── advisor/
        │   │   └── ExecutionAuditAdvisor.java # Token Tracking & Execution Latency Interceptor
        │   ├── audiotranscription/
        │   │   ├── controller/              # Audio Transcription REST Controller
        │   │   └── service/                 # OpenAI Whisper Integration Service
        │   ├── config/
        │   │   ├── AiConfig.java            # ChatClient, Advisors, and Vector Store Beans
        │   │   ├── OpenApiConfig.java       # Swagger / OpenAPI Specification
        │   │   └── RagProperties.java       # Configuration Properties Binding
        │   ├── controller/
        │   │   ├── ApiExceptionHandler.java # Global REST Exception Handler
        │   │   ├── ChatController.java      # Chat, Memory, RAG, and Streaming Endpoints
        │   │   ├── KnowledgeController.java # Qdrant Document Ingestion & Search
        │   │   └── ToolChatController.java  # Function Calling Endpoints
        │   ├── dto/                         # Request & Response Data Transfer Objects
        │   ├── etl/                         # Document ETL Pipeline
        │   │   ├── config/                  # ETL Configuration
        │   │   ├── controller/              # ETL REST Controller
        │   │   ├── loader/                  # Vector Store Ingestion Loader
        │   │   ├── model/                   # Supported Document Types
        │   │   ├── reader/                  # PDF, JSON, DOCX, Markdown, HTML Readers
        │   │   ├── service/                 # ETL Orchestration Service
        │   │   └── transformer/             # Token-based Text Splitter & Chunkers
        │   ├── imagegeneration/
        │   │   ├── controller/              # Image Generation & Download Controller
        │   │   ├── dto/                     # Image Request & Response Models
        │   │   └── service/                 # OpenAI ImageModel Service
        │   ├── mapper/                      # Chat and Document Response Mappers
        │   ├── mcp/
        │   │   ├── internal/                # 🔵 In-Process MCP Server & Client
        │   │   │   ├── client/              # ProductMcpChatService & Controller
        │   │   │   ├── model/               # Product MongoDB Entity
        │   │   │   ├── repository/          # Product MongoDB Repository
        │   │   │   ├── server/              # ProductMcpTools (@McpTool definitions)
        │   │   │   └── service/             # Product Business Logic
        │   │   └── external/                # 🟢 External MCP Client (Draw.io via stdio)
        │   │       ├── DrawioMcpChatService.java
        │   │       └── DrawioMcpController.java
        │   ├── prompt/                      # Dynamic Prompt Templates & Factory
        │   ├── service/                     # Chat, Knowledge, and Streaming Services
        │   ├── tools/                       # DateTime, Calculator, and REST API Tools
        │   └── validation/                  # SafeGuard & Input Validation Rules
        └── resources/
            ├── application.yaml             # Application Configuration & Model Settings
            ├── audio/                       # Sample Audio Files for Whisper Testing
            └── documents/                   # Sample ETL Documents (PDF, JSON, HTML, etc.)
```

---

## 🧩 Deep Dive: Model Context Protocol (MCP)

### Internal vs External MCP Architecture

```
┌────────────────────────────────────────────────────────────────────────┐
│                        Spring Boot Application                         │
│                                                                        │
│  ┌──────────────────────────────────────────────┐                      │
│  │          INTERNAL MCP (In-Process)           │                      │
│  │                                              │                      │
│  │  ProductMcpController                        │                      │
│  │       │                                      │                      │
│  │       ▼                                      │                      │
│  │  ProductMcpChatService ──► ChatClient        │                      │
│  │       │                          │           │                      │
│  │       │                          ▼           │                      │
│  │       │                     OpenAI API       │                      │
│  │       │                          │           │                      │
│  │       │               (Tool Decided)         │                      │
│  │       │                          ▼           │                      │
│  │       └─────────────► ProductMcpTools        │                      │
│  │                       (addProduct, etc.)     │                      │
│  │                              │               │                      │
│  │                              ▼               │                      │
│  │                           MongoDB            │                      │
│  └──────────────────────────────────────────────┘                      │
│                                                                        │
│  ┌──────────────────────────────────────────────┐                      │
│  │      EXTERNAL MCP CLIENT (Draw.io via stdio) │                      │
│  │                                              │                      │
│  │  DrawioMcpController                         │                      │
│  │       │                                      │                      │
│  │       ▼                                      │                      │
│  │  DrawioMcpChatService ──► ChatClient         │                      │
│  │                    SyncMcpToolCallbackProvider                      │
│  │                                │             │                      │
│  │                                ▼             │                      │
│  │                           OpenAI API         │                      │
│  │                                │             │                      │
│  │                         (Tool Decided)       │                      │
│  │                                │             │                      │
│  │                         stdio transport      │                      │
│  │                                ▼             │                      │
│  │                     @drawio/mcp (npx)        │                      │
│  │                     (External Subprocess)    │                      │
│  └──────────────────────────────────────────────┘                      │
└────────────────────────────────────────────────────────────────────────┘
```

---

## 🛡️ Governance & Advisors

Spring AI provides an interceptor/advisor pipeline chained around every `ChatClient` call:
1. **`MessageChatMemoryAdvisor`**: Automatically retrieves preceding conversation turns from MongoDB and appends the latest turn upon completion.
2. **`SafeGuardAdvisor`**: Intercepts input prompts and immediately returns a configured failure message if prohibited keywords are detected (configured under `app.ai.safeguard.sensitive-words`).
3. **`SimpleLoggerAdvisor`**: Logs outgoing prompts and model responses for debugging.
4. **`ExecutionAuditAdvisor`**: Custom audit advisor recording token usage (`promptTokens`, `generationTokens`, `totalTokens`) and total call execution duration in milliseconds.

---

## 🛠️ Troubleshooting & Gotchas

1. **OpenAI Image Generation Error (`400: Unknown parameter: 'response_format'` or Invalid Model)**:
   - Modern OpenAI models like `gpt-image-1-mini` and `gpt-image-1` return `b64_json` by default and reject the legacy `response_format` parameter.
   - Quality options for `gpt-image-1-mini` must be `low`, `medium`, `high`, or `auto` (not `standard`).
   - The application handles base64 decoding and binary streaming automatically.

2. **Qdrant Vector Database Connection (`localhost:6334`)**:
   - If Qdrant is not running locally, ensure `spring.ai.vectorstore.qdrant.initialize-schema: false` is configured in `application.yaml` so startup is not blocked.
   - Start Qdrant with: `docker run -d -p 6333:6333 -p 6334:6334 qdrant/qdrant:latest`.

3. **MongoDB Connection (`localhost:27017`)**:
   - Start MongoDB with: `docker run -d -p 27017:27017 mongo:latest`.
   - Conversational chat memory and product catalog persist in the `spring-ai` database.

4. **Draw.io MCP Server (`npx: command not found`)**:
   - Verify Node.js is installed (`node -v`, `npx -v`).
   - Test manual execution: `npx -y @drawio/mcp`.

5. **`NoClassDefFoundError: TypeInformation` on Startup**:
   - Keep `spring.ai.mcp.client.annotation-scanner.enabled: false` in `application.yaml` to avoid unnecessary classpath scanning conflicts.
