package com.learning.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI springAiOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Spring AI Learning API")
                        .description("REST API Documentation for Spring AI capabilities including Chat Memory, RAG (Retrieval-Augmented Generation), Advanced Contextual RAG, Tool / Function Calling, Document ETL Pipeline, Qdrant Vector Store, and Whisper Audio Transcription.")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Spring AI Learning Team")
                                .url("https://github.com/spring-projects/spring-ai"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local Development Server")
                ))
                .tags(List.of(
                        new Tag().name("Chat API").description("Core Chat endpoints with Mongo chat memory, basic & advanced RAG, dynamic prompts, and SSE streaming"),
                        new Tag().name("Tool Calling API").description("Endpoints demonstrating Spring AI Tool / Function Calling (DateTime, Calculator, JSONPlaceholder User APIs)"),
                        new Tag().name("Knowledge Base API").description("Endpoints for storing and searching vector embeddings in Qdrant Vector Store"),
                        new Tag().name("ETL Pipeline API").description("Batch document ingestion pipeline supporting PDF, JSON, DOCX, Markdown, Text, and HTML"),
                        new Tag().name("Audio Transcription API").description("Audio speech-to-text transcription powered by OpenAI Whisper")
                ));
    }
}
