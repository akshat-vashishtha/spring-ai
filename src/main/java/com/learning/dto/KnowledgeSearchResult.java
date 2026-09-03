package com.learning.dto;

import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Vector search match result with similarity score")
public record KnowledgeSearchResult(
        @Schema(description = "Unique document ID in the vector store", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        String id,

        @Schema(description = "Matching text content retrieved from the vector store", example = "Spring AI provides abstractions for developing AI-powered applications.")
        String content,

        @Schema(description = "Similarity score (higher indicates greater semantic similarity)", example = "0.895")
        Double score,

        @Schema(description = "Metadata associated with the matched document chunk", example = "{\"category\": \"framework\", \"source\": \"internal-docs\"}")
        Map<String, Object> metadata) {
}
