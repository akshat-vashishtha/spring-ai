package com.learning.mcp.external;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.learning.dto.ApiResponse;
import com.learning.dto.ChatResponseDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * Controller exposing endpoints to interact with official Draw.io MCP Server.
 */
@RestController
@RequestMapping("/api/mcp/external/drawio")
@RequiredArgsConstructor
@Tag(name = "External Draw.io MCP API", description = "Endpoints for generating and editing Draw.io diagrams using the official Draw.io MCP Server (@drawio/mcp)")
public class DrawioMcpController {

    private final DrawioMcpChatService drawioMcpChatService;

    @GetMapping("/generate")
    @Operation(summary = "Generate Draw.io Diagram via MCP Tool", description = "Asks AI model to design an architecture or flow diagram and invoke the Draw.io MCP Server (@drawio/mcp) to create the diagram.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Diagram generated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Missing or blank prompt/conversationId"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "AI model / Draw.io tool error")
    })
    public ResponseEntity<ApiResponse<ChatResponseDto>> generateDiagram(
            @Parameter(description = "Prompt describing the diagram to create", example = "Generate an AWS cloud architecture diagram for an e-commerce microservices platform with API Gateway, Cognito Auth, Order Service with DynamoDB, and Product Service with MongoDB")
            @RequestParam String prompt,
            @Parameter(description = "Conversation ID for session memory retention", example = "drawio-session-1")
            @RequestParam String conversationId) {
        ChatResponseDto response = drawioMcpChatService.generateDiagram(prompt, conversationId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/tools")
    @Operation(summary = "List Discovered Draw.io MCP Tools", description = "Returns all tools and schemas provided by the official Draw.io MCP Server.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "List of Draw.io MCP tools retrieved successfully")
    })
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> listDrawioTools() {
        return ResponseEntity.ok(ApiResponse.success(drawioMcpChatService.listDrawioTools()));
    }
}
