# Spring AI Showcase & Reference Architecture 🚀

[![Java](https://img.shields.io/badge/Java-21-orange.svg?logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.8-brightgreen.svg?logo=springboot)](https://spring.io/projects/spring-boot)
[![Spring AI](https://img.shields.io/badge/Spring%20AI-2.0.1-6DB33F.svg?logo=spring)](https://spring.io/projects/spring-ai)
[![Qdrant](https://img.shields.io/badge/Qdrant-Vector%20Store-red.svg?logo=qdrant)](https://qdrant.tech/)
[![MongoDB](https://img.shields.io/badge/MongoDB-Chat%20Memory-green.svg?logo=mongodb)](https://www.mongodb.com/)
[![OpenAPI](https://img.shields.io/badge/OpenAPI-3.0-blue.svg?logo=swagger)](http://localhost:8080/swagger-ui.html)

A production-grade **Spring Boot (4.0.8)** reference application demonstrating the capabilities of **Spring AI (2.0.1)**. This project showcases multi-turn conversational memory, modular Retrieval-Augmented Generation (Basic & Advanced Contextual RAG), autonomous tool / function calling, document ETL ingestion powered by Apache Tika and token splitters, OpenAI Whisper speech-to-text, DALL-E image generation & streaming downloads, and **Model Context Protocol (MCP)** integration both as an in-process tool server and an external stdio client.

---

## 📑 Table of Contents

- [🌟 System Architecture](#-system-architecture)
- [✨ Key Features](#-key-features)
- [📋 Prerequisites](#-prerequisites)
- [🐳 Infrastructure Setup (Docker)](#-infrastructure-setup-docker)
- [⚙️ Configuration & Environment Variables](#️-configuration--environment-variables)
- [🚀 Quick Start Guide](#-quick-start-guide)
- [📖 Interactive OpenAPI & Swagger UI](#-interactive-openapi--swagger-ui)
- [📡 API Reference & Testing Guide](#-api-reference--testing-guide)
  - [1. Chat & Conversational Memory API (`/api/chat`)](#1-chat--conversational-memory-api-apichat)
  - [2. Autonomous Tool Calling API (`/api/chat/tools`)](#2-autonomous-tool-calling-api-apichattools)
  - [3. Knowledge Base & Vector Search API (`/api/knowledge`)](#3-knowledge-base--vector-search-api-apiknowledge)
  - [4. Dedicated RAG API (`/api/rag`)](#4-dedicated-rag-api-apirag)
  - [5. Document ETL Ingestion Pipeline API (`/api/etl`)](#5-document-etl-ingestion-pipeline-api-apietl)
  - [6. Audio Transcription API (`/api/audio/transcription`)](#6-audio-transcription-api-apiaudiotranscription)
  - [7. Image Generation & Direct Download API (`/api/image`)](#7-image-generation--direct-download-api-apiimage)
  - [8. Internal MCP Server & Client (`/api/mcp/internal`)](#8-internal-mcp-server--client-apimcpinternal)
  - [9. External MCP Client — Draw.io (`/api/mcp/external/drawio`)](#9-external-mcp-client--drawio-apimcpexternaldrawio)
- [📁 Project Structure](#-project-structure)
- [🧩 Deep Dive: Model Context Protocol (MCP)](#-deep-dive-model-context-protocol-mcp)
- [🛡️ Governance, Advisors & Safety](#️-governance-advisors--safety)
- [🛠️ Troubleshooting & FAQs](#️-troubleshooting--faqs)

---

## 🌟 System Architecture

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

| Capability | Technology / Module | Highlights |
|---|---|---|
| **Conversational Memory** | MongoDB + `MessageChatMemoryAdvisor` | Thread-safe, multi-turn stateful chats indexed by `conversationId`. |
| **Basic RAG** | `QuestionAnswerAdvisor` + Qdrant | Augments prompts with vector similarity search results. |
| **Advanced RAG** | `RetrievalAugmentationAdvisor` | Dynamic query expansion and re-ranking for enterprise retrieval precision. |
| **Function / Tool Calling** | Spring AI Function Calling | Local arithmetic (`CalculatorTools`), datetime (`DateTimeTools`), and external REST (`UserApiTools`). |
| **Document ETL Pipeline** | Spring AI Apache Tika + `TokenTextSplitter` | Dedicated multipart upload for PDF/DOCX and folder ingestion directly into Qdrant. |
| **Speech-to-Text** | OpenAI Whisper (`whisper-1`) | High-accuracy transcription of custom or sample audio files. |
| **AI Image Studio** | OpenAI Image API (`gpt-image-1-mini`) | Direct byte streaming for inline browser rendering and attachment downloads. |
| **Internal MCP Server** | Spring AI WebMVC MCP Server | Exposes MongoDB Product CRUD tools via Model Context Protocol. |
| **External MCP Client** | Spring AI MCP Client (stdio) | Spawns `@drawio/mcp` subprocess to autonomously generate editable diagrams. |
| **Advisors & Auditing** | Custom Execution Interceptors | Token usage tracking, execution latency logging, and content safety filters. |

---

## 📋 Prerequisites

Ensure the following runtimes and tools are installed:

- **Java 21** or later (`java -version`)
- **Maven 3.9+** (`mvn -version`)
- **Docker** & **Docker Compose** (for MongoDB and Qdrant)
- **Node.js 18+** with `npx` (required for Draw.io MCP client: `npx -y @drawio/mcp`)
- **OpenAI API Key** ([platform.openai.com](https://platform.openai.com/))

---

## 🐳 Infrastructure Setup (Docker)

Start the backing database services with Docker:

```bash
# 1. Start MongoDB (Port 27017) for Conversational Memory and Product Catalog
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

Verify that both containers are healthy:
```bash
docker ps
```

---

## ⚙️ Configuration & Environment Variables

Set your OpenAI API key as an environment variable or define it in `application.yaml`:

```bash
export SPRING_AI_OPENAI_API_KEY="sk-proj-your-actual-api-key"
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

app:
  ai:
    retrieval:
      top-k: 4
      similarity-threshold: 0.5
    safeguard:
      sensitive-words:
        - abusive
        - vulgar
        - violence
        - hate
        - attack
        - kill
        - bomb
        - terror
        - illegal
        - weapon
      failure-response: "I cannot fulfill this request because it contains prohibited or sensitive content."
```

---

## 🚀 Quick Start Guide

### 1. Compile the Project
```bash
mvn clean compile
```

### 2. Launch the Application
```bash
mvn spring-boot:run
```

Once started, the application listens on `http://localhost:8080`.

**Healthy Startup Console Indicators:**
```text
McpServerAutoConfiguration : Registered tools: 5                    [Internal Product MCP Server Active]
StdioClientTransport       : Starting process: [npx, -y, @drawio/mcp] [External Draw.io MCP Connected]
Tomcat started on port 8080 (http) with context path '/'
Started SpringAiApplication in ~3.5 seconds
```

---

## 📖 Interactive OpenAPI & Swagger UI

Explore and test all 24+ REST endpoints interactively in your browser:

- 👉 **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- 👉 **OpenAPI 3.0 JSON Spec**: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

---

## 📡 API Reference & Testing Guide

All response payloads (except streaming and raw binary endpoints) use the standard envelope model `ApiResponse<T>`:
```json
{
  "success": true,
  "data": { ... },
  "error": null
}
```

---

### 1. Chat & Conversational Memory API (`/api/chat`)

Handled by `com.learning.chat.controller.ChatController`.

#### A. Stateless Prompt Completion
```bash
curl -X GET "http://localhost:8080/api/chat?prompt=Explain+polymorphism+in+one+sentence"
```

#### B. Multi-Turn Stateful Chat with Memory (MongoDB)
Maintains conversation history per `conversationId`:
```bash
# Turn 1: Save state
curl -X GET "http://localhost:8080/api/chat/ask?prompt=My+favorite+language+is+Java&conversationId=session-101"

# Turn 2: Recall context
curl -X GET "http://localhost:8080/api/chat/ask?prompt=What+is+my+favorite+language?&conversationId=session-101"
```

#### C. Content Safeguard Interception
When sensitive keywords (e.g., `kill`, `attack`) are detected, the `SafeGuardAdvisor` refuses the request before model invocation:
```bash
curl -X GET "http://localhost:8080/api/chat/ask?prompt=How+to+kill+a+system+process&conversationId=session-101"
```

#### D. Dynamic Prompt Templating
Renders prompt templates with dynamic variables (`persona`, `tone`, `language`, `format`, `topic`):
```bash
curl -X POST "http://localhost:8080/api/chat/dynamic?conversationId=session-101" \
  -H "Content-Type: application/json" \
  -d '{
    "persona": "Software Architect",
    "tone": "concise",
    "language": "English",
    "topic": "Hexagonal Architecture",
    "format": "markdown",
    "additionalInstructions": "Keep it brief"
  }'
```

#### E. Real-Time Token Streaming (Server-Sent Events)
Streams tokens chunk-by-chunk in real-time:
```bash
curl -N -X GET "http://localhost:8080/api/chat/streaming?prompt=Count+from+1+to+5&conversationId=stream-1"
```

---

### 2. Autonomous Tool Calling API (`/api/chat/tools`)

Handled by `com.learning.tools.controller.ToolChatController`. The AI model inspects the user prompt and autonomously determines which registered Spring AI tool functions to execute.

#### A. Local Math & Date Tools
Uses `CalculatorTools` and `DateTimeTools`:
```bash
curl -X GET "http://localhost:8080/api/chat/tools/normal?prompt=What+is+25+percent+of+400?&conversationId=tool-1"
```

#### B. External REST API Tool
Calls `UserApiTools` to query JSONPlaceholder REST API:
```bash
curl -X GET "http://localhost:8080/api/chat/tools/users?prompt=Find+user+with+id+1&conversationId=tool-1"
```

#### C. Multi-Tool Chaining
Model chains local calculation and external user lookups in a single turn:
```bash
curl -X GET "http://localhost:8080/api/chat/tools/all?prompt=Calculate+50+plus+50+and+tell+me+the+city+of+user+1&conversationId=tool-1"
```

---

### 3. Knowledge Base & Vector Search API (`/api/knowledge`)

Handled by `com.learning.rag.controller.KnowledgeController`.

#### A. Ingest Knowledge Document
Generates vector embeddings via OpenAI `text-embedding-3-small` (1536 dimensions) and stores the vector in Qdrant:
```bash
curl -X POST "http://localhost:8080/api/knowledge/documents" \
  -H "Content-Type: application/json" \
  -d '{
    "content": "Spring AI provides high-level portable AI abstractions across OpenAI, Ollama, Anthropic and Qdrant vector database.",
    "metadata": {
      "topic": "Spring AI",
      "author": "Spring Team"
    }
  }'
```

#### B. Semantic Vector Similarity Search
Queries Qdrant using cosine distance similarity:
```bash
curl -X GET "http://localhost:8080/api/knowledge/search?query=Spring+AI+abstractions+and+Qdrant&topK=3&similarityThreshold=0.5"
```

---

### 4. Dedicated RAG API (`/api/rag`)

Handled by `com.learning.rag.controller.RagController`. Decoupled from core chat controllers for clean architectural separation.

#### A. Basic RAG (`QuestionAnswerAdvisor`)
Injects matching document context retrieved from Qdrant directly into the model prompt:
```bash
curl -X GET "http://localhost:8080/api/rag/ask?prompt=What+portable+abstractions+does+Spring+AI+provide?&conversationId=rag-1"
```

#### B. Advanced Contextual RAG (`RetrievalAugmentationAdvisor`)
Performs dynamic query transformation and multi-stage document retrieval for complex questions:
```bash
curl -X GET "http://localhost:8080/api/rag/ask-advanced?prompt=Summarize+the+capabilities+of+vector+databases&conversationId=rag-1"
```

---

### 5. Document ETL Ingestion Pipeline API (`/api/etl`)

Handled by `com.learning.etl.controller.EtlController`. Built using **Spring AI Apache Tika** and **TokenTextSplitter** (chunk size: 800 tokens, min chars: 350, max chunks: 10,000).

#### A. Dedicated PDF/DOCX Document Upload
Accepts multipart files (`.pdf`, `.docx`, `.doc`), parses text via Apache Tika, chunks tokens semantically, and stores vectors into Qdrant:
```bash
curl -X POST "http://localhost:8080/api/etl/upload" \
  -F "file=@/path/to/sample.pdf"
```

**Response Example:**
```json
{
  "success": true,
  "data": {
    "fileName": "sample.pdf",
    "totalChunks": 3,
    "message": "Successfully parsed, chunked, and stored in vector database."
  },
  "error": null
}
```

#### B. Batch Ingest Directory
Recursively scans and ingests all supported documents (`.pdf`, `.docx`, `.doc`, `.txt`, `.html`, `.json`, `.md`) from a directory path:
```bash
curl -X POST "http://localhost:8080/api/etl/ingest-folder" \
  -d "folderPath=src/main/resources/documents"
```

---

### 6. Audio Transcription API (`/api/audio/transcription`)

Handled by `com.learning.audiotranscription.controller.AudioTranscriptionController`.

#### A. Transcribe Classpath Sample Audio
Transcribes the bundled WAV sample (`src/main/resources/audio/sample.wav`):
```bash
curl -X GET "http://localhost:8080/api/audio/transcription/sample"
```

#### B. Transcribe Custom Uploaded Audio
Accepts any audio file format (`.wav`, `.mp3`, `.m4a`, etc.) with optional prompt and language guidance:
```bash
curl -X POST "http://localhost:8080/api/audio/transcription/upload/custom" \
  -F "file=@/path/to/voice_note.mp3" \
  -F "prompt=Technical discussion on distributed systems" \
  -F "language=en"
```

---

### 7. Image Generation & Direct Download API (`/api/image`)

Handled by `com.learning.imagegeneration.controller.ImageGenerationController`. Generates images using OpenAI image models and streams binary `image/png` bytes directly to the client without saving temporary files to disk.

#### A. Direct Browser Rendering (`inline`)
Paste this URL directly into your web browser to view the generated image rendered immediately on screen:
```
http://localhost:8080/api/image/generate?prompt=A+serene+mountain+lake+at+sunset
```
Or via POST:
```bash
curl -X POST "http://localhost:8080/api/image/generate" \
  -H "Content-Type: application/json" \
  -d '{"prompt": "A modern software architecture diagram"}' \
  -o diagram.png
```

#### B. Direct File Download (`attachment`)
Triggers an immediate file download attachment (`image.png`):
```bash
# Via GET query parameter:
curl "http://localhost:8080/api/image/download?prompt=A+cute+golden+retriever+puppy" -o puppy.png

# Via POST JSON body:
curl -X POST "http://localhost:8080/api/image/download" \
  -H "Content-Type: application/json" \
  -d '{"prompt": "A neon cyber city skyline"}' \
  -o cyber_city.png
```

---

### 8. Internal MCP Server & Client (`/api/mcp/internal`)

Handled by `com.learning.mcp.internal.client.ProductMcpController`.

This endpoint uses an **in-process Spring AI WebMVC MCP Server** (`product-catalog`) exposing 5 MongoDB tools:
- `addProduct`: Creates a new product entity.
- `getProduct`: Finds a product by its ID.
- `getAllProducts`: Lists all catalog items.
- `updateProduct`: Updates product properties (name, price, stock, category).
- `deleteProduct`: Removes a product by ID.

```bash
# Add a product conversationally
curl -X GET "http://localhost:8080/api/mcp/internal/products/chat?prompt=Add+a+MacBook+Pro+M4+for+2499.99+USD+in+Electronics+with+50+stock&conversationId=prod-1"

# List all products
curl -X GET "http://localhost:8080/api/mcp/internal/products/chat?prompt=List+all+products+in+the+catalog&conversationId=prod-1"

# Update price
curl -X GET "http://localhost:8080/api/mcp/internal/products/chat?prompt=Update+price+of+product+ID+<id>+to+2199.99&conversationId=prod-1"

# Delete product
curl -X GET "http://localhost:8080/api/mcp/internal/products/chat?prompt=Delete+product+with+id+<id>&conversationId=prod-1"
```

---

### 9. External MCP Client — Draw.io (`/api/mcp/external/drawio`)

Handled by `com.learning.mcp.external.DrawioMcpController`.

Connects over stdio to the official `@drawio/mcp` server. The model synthesizes valid Draw.io XML and returns a **clickable browser link** that opens the editable diagram directly in the Draw.io editor.

#### A. Generate Architecture Diagram
```bash
curl -X GET "http://localhost:8080/api/mcp/external/drawio/generate?prompt=Generate+a+Client+API-Gateway+Microservices+diagram&conversationId=draw-1"
```

#### B. List Discovered Draw.io MCP Tools
```bash
curl -X GET "http://localhost:8080/api/mcp/external/drawio/tools"
```

---

## 📁 Project Structure

The project strictly adheres to a domain-driven modular package structure:

```text
src/main/java/com/learning/
├── SpringAiApplication.java                  # Main application bootstrap entrypoint
│
├── advisor/
│   └── ExecutionAuditAdvisor.java            # Execution latency & token usage auditing
│
├── audiotranscription/                       # OpenAI Whisper Audio Speech-to-Text
│   ├── controller/
│   │   └── AudioTranscriptionController.java # /api/audio/transcription
│   └── service/
│       └── AudioTranscriptionService.java    # OpenAiAudioTranscriptionModel integration
│
├── chat/                                     # Core Chat & Conversation Management
│   ├── controller/
│   │   └── ChatController.java               # /api/chat, /ask, /dynamic, /streaming
│   ├── dto/
│   │   ├── ChatResponseDto.java              # Chat response payload with token metrics
│   │   └── DynamicPromptRequest.java         # Template parameters DTO
│   ├── mapper/
│   │   └── ChatResponseMapper.java           # Maps Spring AI ChatResponse to DTO
│   ├── prompt/
│   │   └── DynamicPromptFactory.java         # StringTemplate dynamic prompt generator
│   └── service/
│       ├── ChatService.java                  # Core chat & Mongo conversation memory logic
│       └── StreamingChatService.java         # Reactive SSE token streaming
│
├── common/                                   # Cross-Cutting Shared Components
│   └── dto/
│       └── ApiResponse.java                  # Standard envelope: ApiResponse<T>(success, data, error)
│
├── config/
│   ├── AiConfig.java                         # ChatClient, Advisors, and Vector Store beans
│   ├── AiProperties.java                     # Application configuration properties
│   └── OpenApiConfig.java                    # Swagger / OpenAPI documentation beans
│
├── etl/                                      # Document ETL Pipeline (Apache Tika)
│   ├── config/
│   │   └── EtlConfig.java                    # TokenTextSplitter bean configuration
│   ├── controller/
│   │   └── EtlController.java                # /api/etl/upload, /api/etl/ingest-folder
│   ├── dto/
│   │   └── DocumentIngestResponse.java       # Ingestion status response model
│   └── service/
│       └── DocumentEtlService.java           # Tika parsing, chunking, and Qdrant ingestion
│
├── exception/
│   └── ApiExceptionHandler.java              # Global @RestControllerAdvice exception handler
│
├── imagegeneration/                          # OpenAI Image Generation Studio
│   ├── controller/
│   │   └── ImageGenerationController.java    # /api/image/generate, /api/image/download
│   ├── dto/
│   │   ├── ImageRequest.java                 # Image prompt request model
│   │   └── ImageResponseDto.java             # Image metadata response model
│   └── service/
│       └── ImageGenerationService.java       # Direct binary PNG streaming service
│
├── mcp/                                      # Model Context Protocol (MCP) Module
│   ├── internal/                             # 🔵 In-Process MCP Server & Client
│   │   ├── client/
│   │   │   ├── ProductMcpChatService.java
│   │   │   └── ProductMcpController.java     # /api/mcp/internal/products/chat
│   │   ├── model/
│   │   │   └── Product.java                  # MongoDB Product Document
│   │   ├── repository/
│   │   │   └── ProductRepository.java        # Spring Data MongoDB Repository
│   │   ├── server/
│   │   │   └── ProductMcpTools.java          # @McpTool definitions (5 CRUD tools)
│   │   └── service/
│   │       └── ProductService.java           # Product business logic
│   └── external/                             # 🟢 External MCP Client (@drawio/mcp via stdio)
│       ├── DrawioMcpChatService.java         # Draw.io tool execution and diagram generator
│       └── DrawioMcpController.java          # /api/mcp/external/drawio/generate, /tools
│
├── rag/                                      # Retrieval-Augmented Generation (RAG) Module
│   ├── config/
│   │   ├── RagConfig.java                    # Qdrant Vector Store & Advisor beans
│   │   └── RetrievalProperties.java          # TopK and similarity threshold properties
│   ├── controller/
│   │   ├── KnowledgeController.java          # /api/knowledge/documents, /search
│   │   └── RagController.java                # /api/rag/ask, /api/rag/ask-advanced
│   ├── dto/
│   │   ├── KnowledgeDocumentRequest.java     # Knowledge ingestion request
│   │   └── KnowledgeSearchResult.java        # Similarity search match model
│   └── service/
│       ├── KnowledgeService.java             # Similarity search & direct vector indexing
│       └── RagService.java                   # Basic & Advanced Contextual RAG services
│
├── tools/                                    # Autonomous Function / Tool Calling Module
│   ├── api/
│   │   └── UserApiTools.java                 # External REST tool (JSONPlaceholder)
│   ├── config/
│   │   └── ToolConfig.java                   # Tool beans configuration
│   ├── controller/
│   │   └── ToolChatController.java           # /api/chat/tools/normal, /users, /all
│   ├── model/
│   │   └── UserDto.java                      # Tool domain model
│   ├── normal/
│   │   ├── CalculatorTools.java              # Arithmetic evaluation tool
│   │   └── DateTimeTools.java                # Datetime and timezone utilities tool
│   └── service/
│       └── ToolChatService.java              # Function calling orchestration service
│
└── validation/
    └── RequestValidator.java                 # SafeGuard & parameter validation rules
```

---

## 🧩 Deep Dive: Model Context Protocol (MCP)

### Internal vs External MCP Architecture

```text
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

1. **In-Process Server (`product-catalog`)**: Uses Spring AI's WebMVC MCP server integration to expose Spring `@McpTool` beans directly over internal protocol bindings.
2. **External Stdio Client (`drawio`)**: Spawns an external Node.js subprocess (`npx -y @drawio/mcp`) via standard I/O pipes. Discovered tools are bound to `ChatClient` using `SyncMcpToolCallbackProvider`.

---

## 🛡️ Governance, Advisors & Safety

Every call to `ChatClient` passes through a configurable advisor chain:

```
User Request
     │
     ▼
[SafeGuardAdvisor]       ──► Rejects prohibited content immediately
     │
     ▼
[MessageChatMemoryAdvisor] ──► Loads previous turns from MongoDB
     │
     ▼
[SimpleLoggerAdvisor]    ──► Logs outgoing prompts and model responses
     │
     ▼
[ExecutionAuditAdvisor]  ──► Measures call latency and records token counts
     │
     ▼
OpenAI LLM API
```

1. **`SafeGuardAdvisor`**: Evaluates prompt tokens against configurable sensitive terms (`app.ai.safeguard.sensitive-words`). If matched, returns a safe canned refusal without incurring OpenAI LLM costs.
2. **`MessageChatMemoryAdvisor`**: Integrates with MongoDB to store and retrieve conversation history by `conversationId`.
3. **`SimpleLoggerAdvisor`**: Outputs full prompt structures and raw completion tokens for debugging.
4. **`ExecutionAuditAdvisor`**: Intercepts requests and responses to calculate round-trip execution latency and token metrics (`promptTokens`, `completionTokens`, `totalTokens`).

---

## 🛠️ Troubleshooting & FAQs

### 1. OpenAI Image Generation (`Unknown parameter: 'response_format'`)
Modern OpenAI models (`gpt-image-1-mini` and `gpt-image-1`) return `b64_json` by default and reject legacy `response_format` parameters. The application decodes base64 strings and streams raw binary PNG bytes directly to clients.

### 2. Qdrant Connection Issues
- Verify Qdrant is running: `docker ps | grep qdrant`.
- Ensure gRPC port `6334` is reachable: `nc -zv localhost 6334`.
- If starting without Qdrant, keep `spring.ai.vectorstore.qdrant.initialize-schema: false` in `application.yaml`.

### 3. Draw.io MCP Server Initialization
- Ensure Node.js 18+ and `npx` are installed: `npx --version`.
- Test running the MCP server standalone: `npx -y @drawio/mcp`.
- Ensure `spring.ai.mcp.client.annotation-scanner.enabled: false` to avoid unnecessary classpath scanning during boot.

### 4. MongoDB Conversational Memory
- Ensure MongoDB is running on port `27017`.
- Conversation state is saved under collection `spring_ai_chat_memory` in the `spring-ai` database.
