package com.learning.mcp.internal.client;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.learning.chat.dto.ChatResponseDto;
import com.learning.common.dto.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * Controller exposing AI MCP Chat for Internal Product Catalog Management.
 */
@RestController
@RequestMapping("/api/mcp/internal/products")
@RequiredArgsConstructor
@Tag(name = "Internal Product MCP API", description = "Endpoints for AI-driven Product Catalog management via Internal MCP Server Tools (stdio, SYNC)")
public class ProductMcpController {

    private final ProductMcpChatService productMcpChatService;

    @GetMapping("/chat")
    @Operation(summary = "Chat with AI using Internal Product Catalog MCP Tools", description = "Allows natural language interaction to add, search, update, or delete products using internal MCP Server Tools (stdio, SYNC).")
    public ResponseEntity<ApiResponse<ChatResponseDto>> chatWithMcp(
            @Parameter(description = "Prompt asking to perform action on products", example = "Add a new product 'Logitech MX Master 3S Mouse' for 99.99 USD in 'Electronics' with 25 stock")
            @RequestParam String prompt,
            @Parameter(description = "Conversation ID for session memory retention", example = "product-mcp-session-1")
            @RequestParam String conversationId) {
        ChatResponseDto response = productMcpChatService.chatWithProductTools(prompt, conversationId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
