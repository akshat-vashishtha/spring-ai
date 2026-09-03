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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/knowledge")
@RequiredArgsConstructor
@Tag(name = "Knowledge Base API", description = "Endpoints for storing and searching vector embeddings in Qdrant Vector Store")
public class KnowledgeController {

    private final KnowledgeService knowledgeService;

    @PostMapping("/documents")
    @Operation(summary = "Add Document to Vector Store", description = "Converts document text into embeddings using OpenAI embedding model and stores it with metadata in Qdrant vector store.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Document successfully embedded and stored; returns document ID"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid document payload"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Vector store or embedding error")
    })
    public ResponseEntity<ApiResponse<String>> addDocument(
            @RequestBody KnowledgeDocumentRequest request) {
        String documentId = knowledgeService.addDocument(request);
        return ResponseEntity.ok(ApiResponse.success(documentId));
    }

    @GetMapping("/search")
    @Operation(summary = "Semantic Search in Vector Store", description = "Executes cosine similarity search against Qdrant vector store based on the semantic embedding of the query string.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "List of matching documents sorted by similarity score"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Missing or invalid search query"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Search execution failed")
    })
    public ResponseEntity<ApiResponse<List<KnowledgeSearchResult>>> search(
            @Parameter(description = "Semantic search query string", example = "Spring AI architecture overview")
            @RequestParam String query,
            @Parameter(description = "Maximum number of matched chunks to return (default: 4)", example = "4")
            @RequestParam(required = false) Integer topK,
            @Parameter(description = "Minimum cosine similarity threshold (0.0 to 1.0, default: 0.5)", example = "0.5")
            @RequestParam(required = false) Double similarityThreshold) {
        return ResponseEntity.ok(ApiResponse.success(
                knowledgeService.search(query, topK, similarityThreshold)));
    }
}
