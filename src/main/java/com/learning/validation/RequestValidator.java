package com.learning.validation;

import org.springframework.stereotype.Component;

import com.learning.dto.DynamicPromptRequest;
import com.learning.dto.KnowledgeDocumentRequest;

@Component
public class RequestValidator {

    public void validateChatRequest(String prompt, String conversationId) {
        requireText(prompt, "prompt");
        requireText(conversationId, "conversationId");
    }

    public void validateDynamicPrompt(
            DynamicPromptRequest request,
            String conversationId) {
        requireNonNull(request, "request");
        requireText(conversationId, "conversationId");
        requireText(request.getTopic(), "topic");
        requireText(request.getPersona(), "persona");
        requireText(request.getTone(), "tone");
        requireText(request.getLanguage(), "language");
        requireText(request.getFormat(), "format");
        requireText(request.getAdditionalInstructions(), "additionalInstructions");
    }

    public void validateKnowledgeDocument(KnowledgeDocumentRequest request) {
        requireNonNull(request, "request");
        requireText(request.content(), "content");
        if (request.metadata() != null) {
            request.metadata().forEach(this::validateMetadataEntry);
        }
    }

    public void validateSearch(
            String query,
            int topK,
            double similarityThreshold) {
        requireText(query, "query");

        if (topK <= 0) {
            throw new IllegalArgumentException("topK must be greater than zero");
        }
        if (similarityThreshold < 0.0 || similarityThreshold > 1.0) {
            throw new IllegalArgumentException(
                    "similarityThreshold must be between 0 and 1");
        }
    }

    private void requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
    }

    private void requireNonNull(Object value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " must not be null");
        }
    }

    private void validateMetadataEntry(String key, Object value) {
        requireText(key, "metadata key");
        requireNonNull(value, "metadata value");
        if (!(value instanceof String || value instanceof Number || value instanceof Boolean)) {
            throw new IllegalArgumentException(
                    "metadata values must be strings, numbers, or booleans");
        }
    }
}
