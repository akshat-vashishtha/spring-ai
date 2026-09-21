package com.learning.rag.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.learning.chat.dto.ChatResponseDto;
import com.learning.common.dto.ApiResponse;
import com.learning.rag.service.RagService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/rag")
@RequiredArgsConstructor
@Tag(name = "RAG API", description = "Endpoints for Basic and Advanced Retrieval-Augmented Generation")
public class RagController {

    private final RagService ragService;

    @GetMapping("/ask")
    @Operation(summary = "Ask with Basic RAG Knowledge Retrieval",
               description = "Augments the user prompt with context retrieved from the Qdrant vector store before generating an answer.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successful RAG-augmented response"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Missing or invalid parameters"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ApiResponse<ChatResponseDto>> askWithKnowledge(
            @Parameter(description = "User query to search against knowledge base and answer", example = "What are the project requirements?")
            @RequestParam String prompt,
            @Parameter(description = "Conversation ID for session memory retention", example = "session-101")
            @RequestParam String conversationId) {
        ChatResponseDto response = ragService.askWithKnowledge(prompt, conversationId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/ask-advanced")
    @Operation(summary = "Ask with Advanced Contextual RAG",
               description = "Uses query transformation and contextual query augmentation to improve vector similarity retrieval and answer accuracy.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successful Advanced RAG response"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Missing or invalid parameters"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ApiResponse<ChatResponseDto>> askWithKnowledgeAdvanced(
            @Parameter(description = "User query to augment and search against vector store", example = "Summarize the architectural guidelines for microservices")
            @RequestParam String prompt,
            @Parameter(description = "Conversation ID for session memory retention", example = "session-101")
            @RequestParam String conversationId) {
        ChatResponseDto response = ragService.askWithKnowledgeAdvanced(prompt, conversationId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
