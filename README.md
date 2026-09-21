# Spring AI Learning Project

[![Java](https://img.shields.io/badge/Java-21-orange.svg?logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.8-brightgreen.svg?logo=springboot)](https://spring.io/projects/spring-boot)
[![Spring AI](https://img.shields.io/badge/Spring%20AI-2.0.1-6DB33F.svg?logo=spring)](https://spring.io/projects/spring-ai)
[![MongoDB](https://img.shields.io/badge/MongoDB-27017-green.svg?logo=mongodb)](https://www.mongodb.com/)
[![Qdrant](https://img.shields.io/badge/Qdrant-VectorDB-red.svg?logo=qdrant)](https://qdrant.tech/)
[![Redis](https://img.shields.io/badge/Redis-SemanticCache-red.svg?logo=redis)](https://redis.io/)
[![OpenAPI](https://img.shields.io/badge/Swagger-OpenAPI%203.0-blue.svg?logo=swagger)](http://localhost:8080/swagger-ui.html)

A hands-on **Proof of Concept (POC)** project for exploring key **Spring AI (2.0.1)** concepts. It covers conversational memory, RAG, tool calling, document ETL, audio transcription, image generation, semantic caching, and full **Model Context Protocol (MCP)** integration — all within a single Spring Boot application.

> **Note**: This is a learning/POC project, not intended for production use.

---

## Table of Contents

- [System Architecture](#system-architecture)
- [Concepts Covered](#concepts-covered)
- [Prerequisites](#prerequisites)
- [Infrastructure Setup](#infrastructure-setup)
- [Configuration](#configuration)
- [Running the Application](#running-the-application)
- [Swagger UI](#swagger-ui)
- [API Reference](#api-reference)
  - [1. Chat & Conversational Memory](#1-chat--conversational-memory-apichat)
  - [2. Tool Calling](#2-tool-calling-apichattools)
  - [3. Knowledge Base & Vector Search](#3-knowledge-base--vector-search-apiknowledge)
  - [4. RAG (Retrieval-Augmented Generation)](#4-rag-retrieval-augmented-generation-apirag)
  - [5. Document ETL Pipeline](#5-document-etl-pipeline-apietl)
  - [6. Audio Transcription](#6-audio-transcription-apiaudiotranscription)
  - [7. Image Generation](#7-image-generation-apiimage)
  - [8. Semantic Cache](#8-semantic-cache-apiv1semantic-cache)
  - [9. Internal MCP Server & Client](#9-internal-mcp-server--client-apimcpinternalproducts)
  - [10. External MCP Client — DeepWiki](#10-external-mcp-client--deepwiki-apimcpexternaldeepwiki)
  - [11. External MCP Client — Draw.io](#11-external-mcp-client--drawio-apimcpexternaldrawio)
- [Project Structure](#project-structure)
- [MCP Deep Dive](#mcp-deep-dive)
- [Advisor Chain & Safety](#advisor-chain--safety)
- [Troubleshooting](#troubleshooting)

---

## System Architecture

```
                              ┌──────────────────────────────────┐
                              │         OpenAI Platform          │
                              │  Chat: gpt-4o-mini               │
                              │  Embeddings: text-embedding-3    │
                              │  Audio: whisper-1                │
                              │  Images: gpt-image-1-mini        │
                              └──────────────┬───────────────────┘
                                             │
              ┌──────────────┐               │                 ┌──────────────────┐
              │  REST Client │  HTTP         │       gRPC      │  Qdrant Vector   │
              │  Swagger UI  ├───────────────▼─────────────────►  Database (6334) │
              └──────────────┘               │                 └──────────────────┘
                                ┌────────────▼───────────┐
                                │   Spring Boot App      │         ┌──────────────────┐
                                │                        ├─────────► MongoDB (27017)   │
                                │  ChatClient + Advisors │         │ Chat Memory      │
                                │  RAG + ETL Pipeline    │         │ Product Catalog  │
                                │  Tool Calling          │         └──────────────────┘
                                │  Semantic Cache        │
                                │  MCP Server            │         ┌──────────────────┐
                                │  MCP Clients           ├─────────► Redis (6379)      │
                                └──────────┬─────────────┘         │ Semantic Cache   │
                                           │                       └──────────────────┘
                              ┌────────────┴────────────┐
                              │   External MCP Servers  │
                              │  DeepWiki (HTTP)        │
                              │  Draw.io (stdio/npx)    │
                              └─────────────────────────┘
```

---

## Concepts Covered

| # | Concept | Technology Used | What It Does |
|---|---------|-----------------|--------------|
| 1 | **Conversational Memory** | MongoDB + `MessageChatMemoryAdvisor` | Maintains multi-turn chat history per `conversationId` |
| 2 | **Tool / Function Calling** | Spring AI `@Tool` + `@McpTool` | AI autonomously calls Java functions (calculator, datetime, REST API) |
| 3 | **Basic RAG** | `QuestionAnswerAdvisor` + Qdrant | Injects relevant document context into prompts via vector search |
| 4 | **Advanced RAG** | `RetrievalAugmentationAdvisor` | Query expansion + multi-stage retrieval for precise answers |
| 5 | **Document ETL** | Apache Tika + `TokenTextSplitter` | Parses PDF/DOCX → chunks → embeds → stores in Qdrant |
| 6 | **Semantic Cache** | Redis + `SemanticCacheAdvisor` | Caches LLM responses by semantic similarity — avoids redundant API calls |
| 7 | **Speech-to-Text** | OpenAI Whisper | Transcribes audio files to text |
| 8 | **Image Generation** | OpenAI Image API | Generates images and streams them as binary PNG bytes |
| 9 | **MCP Server (Internal)** | Spring AI MCP Server (SYNC) | Exposes MongoDB product CRUD as MCP tools |
| 10 | **MCP Sampling** | `McpSyncServerExchange` + `@McpSampling` | MCP server delegates text generation back to the connected client's LLM |
| 11 | **MCP Elicitation (Human in the Loop)** | `@McpElicitation` + REST Callback | Server asks for user confirmation before performing destructive actions |
| 12 | **MCP Client (External HTTP)** | Streamable HTTP + DeepWiki | Connects to a remote MCP server over HTTP and uses its tools |
| 13 | **MCP Tool Filtering** | `McpToolFilter` + `McpToolValidationFilter` | Validates and allow-lists discovered MCP tools before passing to ChatClient |
| 14 | **MCP Client (External stdio)** | stdio transport + `@drawio/mcp` | Spawns an external Node.js MCP server and uses its tools |
| 15 | **Prompt Templating** | `StringTemplateRenderer` | Dynamic prompt generation using parameterized templates |
| 16 | **Streaming** | SSE + Spring WebFlux | Real-time token streaming via Server-Sent Events |
| 17 | **Advisor Chain** | `SafeGuardAdvisor`, `ExecutionAuditAdvisor` | Content safety filtering + execution latency & token tracking |

---

## Prerequisites

Make sure these are installed before starting:

- **Java 21+** — `java -version`
- **Maven 3.9+** — `mvn -version`
- **Docker & Docker Compose** — for MongoDB, Qdrant, and Redis
- **Node.js 18+ with npx** — required for the Draw.io MCP client: `npx --version`
- **OpenAI API Key** — from [platform.openai.com](https://platform.openai.com/)

---

## Infrastructure Setup

Start the required services using Docker:

```bash
# MongoDB — Chat Memory + Product Catalog (port 27017)
docker run -d --name mongodb -p 27017:27017 mongo:latest

# Qdrant — Vector Database (HTTP: 6333, gRPC: 6334)
docker run -d --name qdrant \
  -p 6333:6333 -p 6334:6334 \
  -v $(pwd)/qdrant_data:/qdrant/storage \
  qdrant/qdrant:latest

# Redis — Semantic Cache (port 6379)
docker run -d --name redis -p 6379:6379 redis:latest
```

Verify all containers are running:
```bash
docker ps
```

---

## Configuration

Create a `.env` file in the project root:

```env
OPENAI_API_KEY=sk-proj-your-actual-key-here
```

### Key settings in `application.yaml`

```yaml
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
      chat.options.model: gpt-4o-mini
      embedding.options.model: text-embedding-3-small
      audio.transcription.model: whisper-1
      image.options.model: gpt-image-1-mini
    mcp:
      server:
        enabled: true
        name: product-catalog
        type: SYNC
      client:
        enabled: true
        type: SYNC
        annotation-scanner.enabled: false   # Keep false to avoid Spring Boot 4 reflection issues
        stdio.connections.drawio:
          command: npx
          args: ["-y", "@drawio/mcp"]
        streamable-http.connections.deepwiki:
          url: https://mcp.deepwiki.com
          endpoint: /mcp

app:
  mcp:
    tools.allowed:
      - read_wiki_structure
      - read_wiki_contents
      - ask_question
    elicitation:
      mode: callback        # Elicitation mode: callback (REST) | console | auto-accept | auto-decline
      timeout-seconds: 120  # How long to wait for user confirmation before timing out
  ai:
    semantic-cache:
      similarity-threshold: 0.80
      prefix: "semantic:cache:"
    safeguard.sensitive-words: [abusive, vulgar, violence, kill, bomb, weapon]
```

---

## Running the Application

```bash
# Load .env and start
export $(cat .env | xargs) && mvn spring-boot:run
```

Once started, the app is accessible at `http://localhost:8080`.

---

## Swagger UI

Explore and test all endpoints interactively:

- **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI Spec**: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

---

## API Reference

All endpoints return a standard response envelope:
```json
{ "success": true, "data": { ... }, "error": null }
```

---

### 1. Chat & Conversational Memory (`/api/chat`)

> **Concept**: Multi-turn stateful conversations. MongoDB stores history per `conversationId` via `MessageChatMemoryAdvisor`.

#### Simple one-shot prompt
```bash
curl "http://localhost:8080/api/chat?prompt=Explain+JVM+in+one+sentence"
```

#### Multi-turn chat with memory
```bash
# Turn 1 — save context
curl "http://localhost:8080/api/chat/ask?prompt=I+prefer+Java+over+Python&conversationId=session-1"

# Turn 2 — recall context
curl "http://localhost:8080/api/chat/ask?prompt=Which+language+do+I+prefer?&conversationId=session-1"
```

#### Dynamic prompt template
```bash
curl -X POST "http://localhost:8080/api/chat/dynamic?conversationId=session-1" \
  -H "Content-Type: application/json" \
  -d '{
    "persona": "Software Architect",
    "tone": "concise",
    "language": "English",
    "topic": "Hexagonal Architecture",
    "format": "markdown",
    "additionalInstructions": "Use bullet points"
  }'
```

#### Real-time streaming (SSE)
```bash
curl -N "http://localhost:8080/api/chat/streaming?prompt=Count+from+1+to+5&conversationId=stream-1"
```

#### Content safety (SafeGuardAdvisor)
```bash
# Blocked by sensitive word filter — no LLM call is made
curl "http://localhost:8080/api/chat/ask?prompt=How+to+make+a+bomb&conversationId=session-1"
```

---

### 2. Tool Calling (`/api/chat/tools`)

> **Concept**: The AI model autonomously decides which registered tool functions to call based on the prompt. Tools are Spring beans annotated with `@Tool`.

#### Local tools (calculator + datetime)
```bash
curl "http://localhost:8080/api/chat/tools/normal?prompt=What+is+25+percent+of+400?&conversationId=tool-1"
```

#### External REST API tool (calls JSONPlaceholder)
```bash
curl "http://localhost:8080/api/chat/tools/users?prompt=Find+user+with+id+3&conversationId=tool-1"
```

#### Multi-tool chaining
```bash
curl "http://localhost:8080/api/chat/tools/all?prompt=Add+50+and+50+and+also+tell+me+the+city+of+user+1&conversationId=tool-1"
```

---

### 3. Knowledge Base & Vector Search (`/api/knowledge`)

> **Concept**: Converts text into vector embeddings using OpenAI `text-embedding-3-small` (1536 dimensions) and stores them in Qdrant.

#### Add a document to the vector store
```bash
curl -X POST "http://localhost:8080/api/knowledge/documents" \
  -H "Content-Type: application/json" \
  -d '{
    "content": "Spring AI provides portable AI abstractions across OpenAI, Ollama, and Qdrant.",
    "metadata": { "topic": "Spring AI" }
  }'
```

#### Semantic similarity search
```bash
curl "http://localhost:8080/api/knowledge/search?query=Spring+AI+abstractions&topK=3&similarityThreshold=0.5"
```

---

### 4. RAG (Retrieval-Augmented Generation) (`/api/rag`)

> **Concept**: Augments AI answers with retrieved document context from Qdrant. Two levels: Basic (direct retrieval) and Advanced (query expansion + re-ranking).

#### Basic RAG — `QuestionAnswerAdvisor`
```bash
curl "http://localhost:8080/api/rag/ask?prompt=What+is+Spring+AI?&conversationId=rag-1"
```

#### Advanced RAG — `RetrievalAugmentationAdvisor`
Query rewriting + multi-stage retrieval for richer context:
```bash
curl "http://localhost:8080/api/rag/ask-advanced?prompt=Summarize+vector+database+capabilities&conversationId=rag-1"
```

---

### 5. Document ETL Pipeline (`/api/etl`)

> **Concept**: Full Extract-Transform-Load pipeline. Apache Tika extracts text from documents, `TokenTextSplitter` chunks it (800 tokens/chunk), OpenAI generates embeddings, and Qdrant stores them.

#### Upload a PDF or DOCX
```bash
curl -X POST "http://localhost:8080/api/etl/upload" \
  -F "file=@/path/to/document.pdf"
```

Response:
```json
{ "success": true, "data": { "fileName": "document.pdf", "totalChunks": 8, "message": "..." } }
```

#### Batch ingest a folder
```bash
curl -X POST "http://localhost:8080/api/etl/ingest-folder" \
  -d "folderPath=src/main/resources/documents"
```

Supported formats: `.pdf`, `.docx`, `.doc`, `.txt`, `.html`, `.json`, `.md`

---

### 6. Audio Transcription (`/api/audio/transcription`)

> **Concept**: Uses OpenAI Whisper (`whisper-1`) to convert speech to text.

#### Transcribe the bundled sample audio
```bash
curl "http://localhost:8080/api/audio/transcription/sample"
```

#### Transcribe a custom audio file
```bash
curl -X POST "http://localhost:8080/api/audio/transcription/upload/custom" \
  -F "file=@/path/to/recording.mp3" \
  -F "language=en"
```

---

### 7. Image Generation (`/api/image`)

> **Concept**: Generates images using `gpt-image-1-mini` and streams raw PNG bytes directly to the client — no temp files saved on disk.

#### Render inline in browser (paste URL in browser)
```
http://localhost:8080/api/image/generate?prompt=A+mountain+lake+at+sunset
```

#### Download as file
```bash
curl "http://localhost:8080/api/image/download?prompt=A+futuristic+city+at+night" -o city.png
```

---

### 8. Semantic Cache (`/api/v1/semantic-cache`)

> **Concept**: Redis-backed `SemanticCacheAdvisor` intercepts `ChatClient` calls. Before hitting OpenAI, it checks if a semantically similar prompt was answered recently (cosine similarity >= 0.80). If a cache hit is found, the cached response is returned instantly — saving latency and API cost.

#### Clear the cache
```bash
curl -X DELETE "http://localhost:8080/api/v1/semantic-cache/clear"
```

**How it works:**
1. First call: No cache hit → OpenAI is called → response stored in Redis with embedding.
2. Same (or very similar) question again → Cache hit → cached response returned without calling OpenAI.

---

### 9. Internal MCP Server & Client (`/api/mcp/internal/products`)

> **Concept**: An in-process **MCP Server** (`product-catalog`) is embedded inside this same Spring Boot app. It exposes MongoDB product CRUD operations as MCP tools using `@McpTool`. A `ChatClient` in the same app acts as the MCP client.

#### Available tools registered on the MCP server

| Tool | Description |
|------|-------------|
| `addProduct` | Create a new product |
| `getProduct` | Find product by ID |
| `getAllProducts` | List all products |
| `updateProduct` | Update product fields |
| `deleteProduct` | Remove a product — requires user confirmation via **MCP Elicitation** |
| `generateProductDescription` | **MCP Sampling** — asks the client LLM to write a marketing description |
| `summarizeCatalog` | **MCP Sampling** — asks the client LLM to summarize the catalog |

```bash
# Add a product via natural language
curl "http://localhost:8080/api/mcp/internal/products/chat?prompt=Add+a+MacBook+Pro+M4+for+2499.99+in+Electronics+with+50+stock&conversationId=prod-1"

# List all products
curl "http://localhost:8080/api/mcp/internal/products/chat?prompt=List+all+products&conversationId=prod-1"

# Delete a product — triggers MCP Elicitation, waits for user confirmation
curl "http://localhost:8080/api/mcp/internal/products/chat?prompt=Delete+product+with+id+<id>&conversationId=prod-1"

# MCP Sampling — generate a product description using the client's LLM
curl "http://localhost:8080/api/mcp/internal/products/chat?prompt=Generate+a+description+for+product+id+<id>&conversationId=prod-1"

# MCP Sampling — summarize the entire catalog
curl "http://localhost:8080/api/mcp/internal/products/chat?prompt=Summarize+the+product+catalog&conversationId=prod-1"
```

---

### 10. External MCP Client — DeepWiki (`/api/mcp/external/deepwiki`)

> **Concept**: Connects to the remote **DeepWiki MCP Server** over **Streamable HTTP** transport. Tool discovery, validation filtering, and execution all happen over the wire. `McpToolValidationFilter` ensures only approved tools are passed to `ChatClient`.

#### List filtered tools (only allow-listed tools shown)
```bash
curl "http://localhost:8080/api/mcp/external/deepwiki/tools"
```

Response (filtered from all available tools):
```json
[
  { "name": "read_wiki_structure", "approved": true },
  { "name": "read_wiki_contents",  "approved": true },
  { "name": "ask_question",        "approved": true }
]
```

#### Chat using DeepWiki tools
```bash
curl "http://localhost:8080/api/mcp/external/deepwiki/chat?prompt=What+topics+are+in+facebook/react+wiki&conversationId=wiki-1"
```

#### Chat with custom tool allow-list for this request only
```bash
curl "http://localhost:8080/api/mcp/external/deepwiki/chat?prompt=Get+the+wiki+structure+of+spring-projects/spring-ai&conversationId=wiki-1&allowedTools=read_wiki_structure"
```

---

### 11. External MCP Client — Draw.io (`/api/mcp/external/drawio`)

> **Concept**: Spawns `@drawio/mcp` as an external Node.js subprocess over **stdio transport**. The AI model uses its tools to create editable Draw.io diagrams and returns a direct browser link.

#### Generate a diagram
```bash
curl "http://localhost:8080/api/mcp/external/drawio/generate?prompt=Create+an+AWS+microservices+architecture+with+API+Gateway+and+MongoDB&conversationId=draw-1"
```

Response includes a clickable link that opens the diagram directly in the Draw.io editor.

#### List available Draw.io tools
```bash
curl "http://localhost:8080/api/mcp/external/drawio/tools"
```

---

## Project Structure

```text
src/main/java/com/learning/
├── SpringAiApplication.java
│
├── advisor/
│   └── ExecutionAuditAdvisor.java          # Latency + token usage tracking
│
├── audiotranscription/
│   ├── controller/AudioTranscriptionController.java
│   └── service/AudioTranscriptionService.java       # Whisper integration
│
├── chat/
│   ├── controller/ChatController.java               # /api/chat
│   ├── dto/                                         # ChatResponseDto, DynamicPromptRequest
│   ├── mapper/ChatResponseMapper.java
│   ├── prompt/DynamicPromptFactory.java             # StringTemplate-based dynamic prompts
│   └── service/
│       ├── ChatService.java                         # Memory-aware chat
│       └── StreamingChatService.java                # SSE token streaming
│
├── common/dto/ApiResponse.java                      # Standard response envelope
│
├── config/
│   ├── AiConfig.java                                # ChatClient + all advisor beans
│   ├── AiProperties.java
│   └── OpenApiConfig.java
│
├── etl/
│   ├── config/EtlConfig.java                        # TokenTextSplitter bean
│   ├── controller/EtlController.java                # /api/etl
│   └── service/DocumentEtlService.java              # Tika parse → chunk → embed → store
│
├── exception/ApiExceptionHandler.java
│
├── imagegeneration/
│   ├── controller/ImageGenerationController.java    # /api/image
│   └── service/ImageGenerationService.java          # Binary PNG byte streaming
│
├── mcp/
│   ├── external/                                    # External MCP Client Integrations
│   │   ├── deepwiki/
│   │   │   ├── DeepWikiMcpChatService.java          # Streamable HTTP MCP client + filtering
│   │   │   └── DeepWikiMcpController.java           # /api/mcp/external/deepwiki
│   │   ├── drawio/
│   │   │   ├── DrawioMcpChatService.java            # stdio MCP client + diagram generation
│   │   │   └── DrawioMcpController.java             # /api/mcp/external/drawio
│   │   └── filter/
│   │       └── McpToolValidationFilter.java         # McpToolFilter — global + per-request allow-list
│   │
│   └── internal/                                    # In-Process MCP Server & Client Architecture
│       ├── model/
│       │   └── Product.java                         # MongoDB document
│       ├── repository/
│       │   └── ProductRepository.java               # Spring Data MongoDB repository
│       ├── service/
│       │   └── ProductService.java                  # Product business logic
│       ├── server/                                  # Server-Side MCP Components
│       │   ├── ProductMcpTools.java                 # CRUD @McpTool methods
│       │   ├── elicitation/
│       │   │   └── DeleteConfirmationElicitor.java  # Server-side: sends elicitation request to client
│       │   └── sampling/
│       │       ├── ProductSamplingPromptBuilder.java
│       │       └── ProductSamplingTools.java        # Server @McpTool delegating to client LLM
│       └── client/                                  # Client-Side MCP Components
│           ├── controller/
│           │   ├── ProductMcpController.java        # /api/mcp/internal/products/chat
│           │   └── ElicitationCallbackController.java # /api/mcp/internal/elicitation/confirm/{id}
│           ├── service/
│           │   └── ProductMcpChatService.java       # ChatClient tool orchestration
│           ├── sampling/
│           │   └── ProductMcpSamplingHandler.java   # Client @McpSampling handler
│           └── elicitation/
│               ├── DeleteConfirmationElicitationHandler.java # Client @McpElicitation handler
│               └── ElicitationSessionStore.java     # Holds pending confirmation sessions
│
├── rag/
│   ├── config/RagConfig.java                        # Qdrant VectorStore + Advisor beans
│   ├── controller/
│   │   ├── KnowledgeController.java                 # /api/knowledge
│   │   └── RagController.java                       # /api/rag
│   └── service/
│       ├── KnowledgeService.java
│       └── RagService.java                          # Basic + Advanced RAG logic
│
├── semanticcache/
│   ├── config/SemanticCacheConfig.java              # Redis-backed SemanticCache bean
│   └── controller/SemanticCacheController.java      # /api/v1/semantic-cache/clear
│
├── tools/
│   ├── api/UserApiTools.java                        # External REST tool (JSONPlaceholder)
│   ├── controller/ToolChatController.java           # /api/chat/tools
│   ├── normal/
│   │   ├── CalculatorTools.java
│   │   └── DateTimeTools.java
│   └── service/ToolChatService.java
│
└── validation/RequestValidator.java
```

---

## MCP Deep Dive

### Transport Types Used

| Client | Transport | Server |
|--------|-----------|--------|
| Internal product-catalog client | In-process (embedded) | Spring AI MCP Server inside same JVM |
| DeepWiki | Streamable HTTP | `https://mcp.deepwiki.com/mcp` (remote, no auth) |
| Draw.io | stdio | `npx -y @drawio/mcp` (local subprocess) |

---

### MCP Tool Filtering (`McpToolValidationFilter`)

When connecting to an external MCP server, it can expose many tools. `McpToolValidationFilter` (implementing `McpToolFilter`) controls which tools are visible to `ChatClient`:

- **Global level**: Tools listed under `app.mcp.tools.allowed` in `application.yaml` are always allowed.
- **Per-request level**: The `/api/mcp/external/deepwiki/chat` endpoint also accepts an `allowedTools` query param to restrict tools dynamically for that single request.

---

### MCP Sampling

MCP Sampling is an MCP protocol feature where the **MCP Server asks the MCP Client's LLM** to generate text, rather than maintaining its own LLM connection.

```
[User prompt] → [ChatClient]
                    │
                    ▼
       [ProductSamplingTools @McpTool]   ← registered on MCP Server
                    │
          1. Fetch product from MongoDB
          2. Build prompt via ProductSamplingPromptBuilder
          3. Check client supports sampling:
             exchange.getClientCapabilities().sampling() != null
                    │
                    ▼
       [exchange.createMessage(request)]  ← Server delegates to Client
                    │
       [ProductMcpSamplingHandler]        ← @McpSampling(clients="product-catalog")
          calls ChatClient → OpenAI → generates text
                    │
                    ▼
       LLM response returned to tool → returned to user
```

Implementation:
- **Server side**: `ProductSamplingTools.java` — uses `McpSyncServerExchange.createMessage()`
- **Client side**: `ProductMcpSamplingHandler.java` — implements `McpClientCustomizer` and `@McpSampling(clients = "product-catalog")`

---

### MCP Elicitation (Human in the Loop)

MCP Elicitation is an MCP protocol feature that lets the **MCP Server pause a tool execution and ask the user a question** before proceeding. This is useful for destructive operations like `deleteProduct` — the server stops and waits for explicit user confirmation before anything is deleted.

#### Why this matters

Without elicitation, the AI would delete a product immediately after being asked. With elicitation, the server sends a confirmation request to the client, and nothing happens until the user explicitly confirms or cancels — either via a UI, a REST call, or any other callback mechanism.

#### How it works in this project (REST Callback mode)

```
User sends: "Delete product with id abc123"
                    │
                    ▼
         [ChatClient calls deleteProduct tool]
                    │
                    ▼
         [DeleteConfirmationElicitor]
           - Checks product exists in MongoDB
           - Sends McpSchema.ElicitFormRequest to the Client
           - Pauses execution, waits for response
                    │
                    ▼
         [DeleteConfirmationElicitationHandler]  ← @McpElicitation (client side)
           - Receives the elicitation request
           - Creates a pending session in ElicitationSessionStore
           - Returns the session ID to the user as a response
                    │
                    ▼
         User receives: { "sessionId": "abc-123", "message": "Confirm deletion?" }
                    │
         User decides: confirm or cancel
                    │
                    ▼
         [POST /api/mcp/internal/elicitation/confirm/{sessionId}]
           body: { "confirm": true }   ← or false to cancel
                    │
                    ▼
         [ElicitationCallbackController]
           - Resolves the pending session in ElicitationSessionStore
                    │
                    ▼
         [DeleteConfirmationElicitor] unblocks
           - confirm=true  → deletes product from MongoDB
           - confirm=false → cancels safely, nothing deleted
```

#### Confirmation API

```bash
# After receiving a sessionId from the chat response:

# Confirm deletion
curl -X POST "http://localhost:8080/api/mcp/internal/elicitation/confirm/{sessionId}" \
  -H "Content-Type: application/json" \
  -d '{ "confirm": true }'

# Cancel deletion
curl -X POST "http://localhost:8080/api/mcp/internal/elicitation/confirm/{sessionId}" \
  -H "Content-Type: application/json" \
  -d '{ "confirm": false }'
```

#### Configuration

```yaml
app:
  mcp:
    elicitation:
      mode: callback        # callback | console | auto-accept | auto-decline
      timeout-seconds: 120  # seconds to wait before timing out
```

| Mode | Behavior |
|------|----------|
| `callback` | Returns a session ID; waits for REST confirmation from the user |
| `console` | Blocks the thread and prompts via terminal input (dev/testing only) |
| `auto-accept` | Automatically confirms all deletions (for automated testing) |
| `auto-decline` | Automatically cancels all deletions (for automated testing) |

Implementation:
- **Server side**: `DeleteConfirmationElicitor.java` — sends elicitation request via `McpSyncServerExchange`
- **Client side**: `DeleteConfirmationElicitationHandler.java` — captures the request with `@McpElicitation`, registers a pending session
- **Session store**: `ElicitationSessionStore.java` — holds pending confirmation futures
- **Callback endpoint**: `ElicitationCallbackController.java` — resolves the session when user responds

---

## Advisor Chain & Safety

Every `ChatClient` call passes through this advisor pipeline (configured in `AiConfig.java`):

```
User Prompt
     │
     ▼
[SafeGuardAdvisor]          → Rejects blocked keywords immediately (no LLM call)
     │
     ▼
[MessageChatMemoryAdvisor]  → Loads/saves conversation history from MongoDB
     │
     ▼
[SemanticCacheAdvisor]      → Returns cached response if semantic match found in Redis
     │
     ▼
[ExecutionAuditAdvisor]     → Measures latency, logs token counts
     │
     ▼
[SimpleLoggerAdvisor]       → Logs full request/response for debugging
     │
     ▼
OpenAI LLM
```

| Advisor | What It Does |
|---------|-------------|
| `SafeGuardAdvisor` | Matches prompt against configurable sensitive words. Returns a canned refusal without incurring API cost. |
| `MessageChatMemoryAdvisor` | Stores and retrieves conversation turns from MongoDB by `conversationId`. |
| `SemanticCacheAdvisor` | Checks Redis for a semantically similar past response (cosine similarity >= 0.80). On cache hit, skips LLM entirely. |
| `ExecutionAuditAdvisor` | Custom advisor that logs round-trip latency in ms and token usage (prompt/completion/total). |
| `SimpleLoggerAdvisor` | Spring AI built-in advisor for full debug logging of prompts and responses. |

---

## Troubleshooting

### App fails to start with `Failed to introspect Class [QuerydslPredicateOperationCustomizer]`
Keep `spring.ai.mcp.client.annotation-scanner.enabled: false` in `application.yaml`. Spring Boot 4 + Springdoc's Querydsl class causes a reflection crash when the scanner is enabled. The sampling handler is registered programmatically via `McpClientCustomizer` instead.

### Circular dependency on startup
`ProductMcpSamplingHandler` uses `@Lazy ChatClient` to break the initialization cycle: `ChatClient → McpToolCallbacks → McpSyncClients → McpClientCustomizer → ChatClient`. The `@Lazy` annotation delays `ChatClient` injection until it's actually needed (at sampling request time).

### Qdrant connection issues
- Check Docker: `docker ps | grep qdrant`
- Test gRPC port: `nc -zv localhost 6334`

### Draw.io MCP not starting
- Check Node.js: `npx --version`
- Test standalone: `npx -y @drawio/mcp`

### MongoDB connection issues
- Check Docker: `docker ps | grep mongodb`
- Default port: `27017`, database: `spring-ai`

### Redis connection issues
- Check Docker: `docker ps | grep redis`
- Default port: `6379`

### OpenAI Image Generation — `Unknown parameter: 'response_format'`
Modern OpenAI image models return `b64_json` by default. The app decodes base64 and streams raw PNG bytes — no `response_format` parameter is sent.
