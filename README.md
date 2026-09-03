# Spring AI Learning & Showcase Project 🚀

A comprehensive Spring Boot application showcasing **Spring AI** capabilities including Conversational Chat Memory, Basic & Advanced RAG (Retrieval-Augmented Generation), Function / Tool Calling, Multi-format Document ETL Pipeline, Vector Store Search (Qdrant), and Speech-to-Text Transcription (OpenAI Whisper).

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
- [Project Structure](#-project-structure)
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
|   Swagger UI /      |  HTTP     |    Spring Boot (Spring AI)  |   GRPC    |    Qdrant Vector     |
|   REST Clients      +---------->| - ChatClient & Advisors     +---------->|    Database          |
+---------------------+           | - ETL Pipeline & Splitters  |  (6334)   | (Embeddings: 1536d)  |
                                  | - Function Calling (Tools)  |           +----------------------+
                                  | - Whisper Audio Service     |
                                  +--------------^--------------+
                                                 | (27017)
                                  +--------------v--------------+
                                  |      MongoDB Database       |
                                  |   (Conversational Memory)   |
                                  +-----------------------------+
```

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
7. **Interactive OpenAPI / Swagger Documentation**: Full interactive Swagger UI available out of the box.

---

## 📋 Prerequisites

Ensure you have the following installed on your machine:
- **Java 21** or later (`java -version`)
- **Maven 3.9+** (`mvn -version`)
- **Docker** / **Docker Compose** (for MongoDB and Qdrant)
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
      api-key: sk-proj-your-actual-api-key
      chat:
        options:
          model: gpt-4o-mini
      embedding:
        options:
          model: text-embedding-3-small
      audio:
        transcription:
          model: whisper-1
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

---

## 📖 Using Swagger UI (OpenAPI)

Once the application is running, open your web browser and navigate to:

👉 **[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)**
*(or [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html))*

### Raw OpenAPI JSON Specification:
👉 **[http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)**

### How to test endpoints in Swagger UI:
1. Open the Swagger UI page in your browser.
2. Click on any section (e.g., **Chat API**, **Tool Calling API**, **Knowledge Base API**).
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

## 📁 Project Structure

```
spring-ai/
├── pom.xml
├── README.md (and Read.md)
└── src/
    └── main/
        ├── java/com/learning/
        │   ├── SpringAiApplication.java
        │   ├── advisor/                 # Custom Token Usage Advisors
        │   ├── audiotranscription/      # Whisper Audio Controller & Service
        │   ├── config/                  # OpenAPI, RAG & AI Configuration
        │   ├── controller/              # REST Controllers (Chat, Tools, Knowledge)
        │   ├── dto/                     # API Request & Response Models
        │   ├── etl/                     # Document Readers, Splitters & Pipeline
        │   ├── mapper/                  # Document & Response Mappers
        │   ├── prompt/                  # Dynamic Prompt Templates
        │   ├── service/                 # Chat, Streaming, Knowledge & Tool Services
        │   ├── tools/                   # DateTime, Calculator & External API Tools
        │   └── validation/              # Safeguard & Input Validators
        └── resources/
            ├── application.yaml         # Application & OpenAPI Configuration
            └── documents/               # Sample ETL documents
```

---

## 🛠️ Troubleshooting

1. **MongoDB Connection Refused (`localhost:27017`)**:
   - Ensure MongoDB Docker container is running: `docker start mongodb` or `docker run -d -p 27017:27017 mongo:latest`.
2. **Qdrant gRPC Connection Error (`localhost:6334`)**:
   - Ensure Qdrant container is running: `docker start qdrant` or `docker run -d -p 6333:6333 -p 6334:6334 qdrant/qdrant:latest`.
3. **OpenAI 401 Unauthorized / Invalid API Key**:
   - Verify your API key is correctly specified in `application.yaml` or exported as `SPRING_AI_OPENAI_API_KEY`.
4. **Swagger UI Not Loading**:
   - Verify the server is running on `http://localhost:8080`.
   - Access via direct URL: `http://localhost:8080/swagger-ui/index.html`.
