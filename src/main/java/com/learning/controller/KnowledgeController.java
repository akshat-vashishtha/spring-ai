package com.learning.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.learning.dto.ApiResponse;
import com.learning.dto.KnowledgeDocumentRequest;
import com.learning.dto.KnowledgeSearchResult;
import com.learning.service.KnowledgeService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/knowledge")
@RequiredArgsConstructor
public class KnowledgeController {

    private final KnowledgeService knowledgeService;

    @PostMapping("/documents")
    public ResponseEntity<ApiResponse<String>> addDocument(
            @RequestBody KnowledgeDocumentRequest request) {
        String documentId = knowledgeService.addDocument(request);
        return ResponseEntity.ok(ApiResponse.success(documentId));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<KnowledgeSearchResult>>> search(
            @RequestParam String query,
            @RequestParam(required = false) Integer topK,
            @RequestParam(required = false) Double similarityThreshold) {
        return ResponseEntity.ok(ApiResponse.success(
                knowledgeService.search(query, topK, similarityThreshold)));
    }
}
