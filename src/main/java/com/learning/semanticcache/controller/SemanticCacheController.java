package com.learning.semanticcache.controller;

import org.springframework.ai.chat.cache.semantic.SemanticCache;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/semantic-cache")
@RequiredArgsConstructor
@Tag(name = "Semantic Cache", description = "Redis Semantic Cache endpoints")
public class SemanticCacheController {

    private final SemanticCache semanticCache;

    @DeleteMapping("/clear")
    @Operation(summary = "Clear semantic cache")
    public ResponseEntity<String> clear() {
        semanticCache.clear();
        return ResponseEntity.ok("Semantic cache cleared successfully");
    }
}
