package com.learning.mcp.external.deepwiki;

import java.util.List;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.learning.chat.dto.ChatResponseDto;
import com.learning.common.dto.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/mcp/external/deepwiki")
@RequiredArgsConstructor
@Tag(name = "External DeepWiki MCP API", description = "Endpoints for interacting with DeepWiki MCP server via Streamable HTTP with tool validation filtering")
public class DeepWikiMcpController {

    private final DeepWikiMcpChatService deepWikiMcpChatService;

    @GetMapping("/chat")
    @Operation(summary = "Ask DeepWiki via Filtered MCP Tools", description = "Sends prompt to AI model equipped with allow-listed DeepWiki MCP tools. Supports optional API-level tool filtering.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Response generated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Missing or blank prompt/conversationId"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "AI model / MCP tool error")
    })
    public ResponseEntity<ApiResponse<ChatResponseDto>> chat(
            @Parameter(description = "Prompt or question for the AI model", example = "Summarize the repository documentation structure")
            @RequestParam String prompt,
            @Parameter(description = "Conversation ID for session memory retention", example = "deepwiki-session-1")
            @RequestParam String conversationId,
            @Parameter(description = "Optional custom list of allowed tools for this request", example = "read_wiki_structure,read_wiki_contents")
            @RequestParam(required = false) List<String> allowedTools) {
        ChatResponseDto response = deepWikiMcpChatService.chat(prompt, conversationId, allowedTools);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Stream DeepWiki AI Response with Filtered MCP Tools", description = "Streams tokens in real-time as SSE from AI model equipped with allow-listed DeepWiki tools. Supports optional API-level tool filtering.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Stream established successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Missing or blank prompt/conversationId")
    })
    public Flux<String> stream(
            @Parameter(description = "Prompt or question for streaming completion", example = "Explain how wiki structure is retrieved")
            @RequestParam String prompt,
            @Parameter(description = "Conversation ID for session memory retention", example = "deepwiki-session-1")
            @RequestParam String conversationId,
            @Parameter(description = "Optional custom list of allowed tools for this request", example = "read_wiki_structure,read_wiki_contents")
            @RequestParam(required = false) List<String> allowedTools) {
        return deepWikiMcpChatService.stream(prompt, conversationId, allowedTools);
    }

    @GetMapping("/tools")
    @Operation(summary = "List Discovered and Filtered DeepWiki MCP Tools", description = "Returns all tools provided by DeepWiki MCP server along with their allow-list validation status.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "List of DeepWiki tools retrieved successfully")
    })
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> listTools() {
        return ResponseEntity.ok(ApiResponse.success(deepWikiMcpChatService.listTools()));
    }
}
