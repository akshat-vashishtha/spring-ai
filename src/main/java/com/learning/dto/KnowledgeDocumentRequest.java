package com.learning.dto;

import java.util.Map;

public record KnowledgeDocumentRequest(
        String content,
        Map<String, Object> metadata) {
}
