package com.learning.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.learning.dto.ApiResponse;
import com.learning.dto.ChatResponseDto;
import com.learning.service.ToolChatService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * REST Controller exposing endpoints demonstrating Spring AI Tool Calling.
 */
@RestController
@RequestMapping("/api/chat/tools")
@RequiredArgsConstructor
@Tag(name = "Tool Calling API", description = "Endpoints demonstrating Spring AI Tool / Function Calling (DateTime, Calculator, JSONPlaceholder User APIs)")
public class ToolChatController {

    private final ToolChatService toolChatService;

    /**
     * Ask question with Normal Tools enabled (DateTime, Calculator).
     */
    @GetMapping("/normal")
    @Operation(summary = "Ask question using Local Tools", description = "Executes function calling with local tool beans: DateTimeTools (current time, timezone offset) and CalculatorTools (math expressions, percentages, unit conversions).")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successful response after tool execution"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Missing required parameters"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "AI model / tool invocation error")
    })
    public ResponseEntity<ApiResponse<ChatResponseDto>> askWithNormalTools(
            @Parameter(description = "Prompt requiring calculation or date/time queries", example = "What is current time in UTC and what is 15 percent of 850?")
            @RequestParam String prompt,
            @Parameter(description = "Conversation ID for session memory retention", example = "tool-session-1")
            @RequestParam String conversationId) {
        return respond(toolChatService.askWithNormalTools(prompt, conversationId));
    }

    /**
     * Ask question with JSONPlaceholder User API Tools enabled.
     */
    @GetMapping("/users")
    @Operation(summary = "Ask question using External REST API Tools", description = "Executes function calling connected to external JSONPlaceholder REST API (fetch user by ID, search users, list all users).")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successful response after external API tool execution"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Missing required parameters"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "AI model / API tool invocation error")
    })
    public ResponseEntity<ApiResponse<ChatResponseDto>> askWithUserApiTools(
            @Parameter(description = "Prompt asking for user information", example = "Give me details and email for user with id 1")
            @RequestParam String prompt,
            @Parameter(description = "Conversation ID for session memory retention", example = "tool-session-1")
            @RequestParam String conversationId) {
        return respond(toolChatService.askWithUserApiTools(prompt, conversationId));
    }

    /**
     * Ask question with All Tools enabled (Normal Tools + JSONPlaceholder API).
     */
    @GetMapping("/all")
    @Operation(summary = "Ask question using All Registered Tools", description = "Enables all local and REST API tools concurrently, allowing the model to choose and chain multiple tools as needed.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successful response after multi-tool execution"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Missing required parameters"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "AI model / tool invocation error")
    })
    public ResponseEntity<ApiResponse<ChatResponseDto>> askWithAllTools(
            @Parameter(description = "Complex prompt requiring both math/datetime and external user tools", example = "Who is user 2 and convert their city temperature 25 Celsius to Fahrenheit?")
            @RequestParam String prompt,
            @Parameter(description = "Conversation ID for session memory retention", example = "tool-session-1")
            @RequestParam String conversationId) {
        return respond(toolChatService.askWithAllTools(prompt, conversationId));
    }

    private ResponseEntity<ApiResponse<ChatResponseDto>> respond(ChatResponseDto result) {
        return result != null
                ? ResponseEntity.ok(ApiResponse.success(result))
                : ResponseEntity.internalServerError()
                        .body(ApiResponse.error("Failed to get response from AI model"));
    }
}
