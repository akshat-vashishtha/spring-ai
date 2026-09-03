package com.learning.dto;

import java.util.Map;

public record KnowledgeSearchResult(
        String id,
        String content,
        Double score,
        Map<String, Object> metadata) {
}
