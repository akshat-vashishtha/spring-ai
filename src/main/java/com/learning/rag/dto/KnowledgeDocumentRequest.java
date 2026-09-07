package com.learning.rag.dto;

import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request to ingest a custom document into the vector store")
public record KnowledgeDocumentRequest(
        @Schema(description = "Text content of the document", example = "Spring AI provides abstractions for developing AI-powered applications.")
        String content,

        @Schema(description = "Key-value metadata associated with the document (e.g., author, category, source)", example = "{\"category\": \"framework\", \"source\": \"internal-docs\"}")
        Map<String, Object> metadata) {
}
