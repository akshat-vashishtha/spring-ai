package com.learning.tools.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.learning.chat.dto.ChatResponseDto;
import com.learning.common.dto.ApiResponse;
import com.learning.tools.service.ToolChatService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * REST Controller exposing endpoints demonstrating Spring AI Tool Calling with ToolContext.
 */
@RestController
@RequestMapping("/api/chat/tools")
@RequiredArgsConstructor
@Tag(name = "Tool Calling API", description = "Endpoints demonstrating Spring AI Tool Calling with ToolContext")
public class ToolChatController {

    private final ToolChatService toolChatService;

    @GetMapping("/normal")
    @Operation(summary = "Ask question using Local Tools (DateTime & Calculator)")
    public ResponseEntity<ApiResponse<ChatResponseDto>> askWithNormalTools(
            @RequestParam String prompt,
            @RequestParam String conversationId,
            @RequestHeader(value = "X-Time-Zone", required = false) String timeZone,
            @RequestHeader(value = "X-Decimal-Precision", required = false) Integer precision) {
        Map<String, Object> context = new HashMap<>();
        if (timeZone != null && !timeZone.isBlank()) context.put("timeZone", timeZone);
        if (precision != null) context.put("decimalPrecision", precision);
        return respond(toolChatService.askWithNormalTools(prompt, conversationId, context));
    }

    @GetMapping("/users")
    @Operation(summary = "Ask question using User API Tools")
    public ResponseEntity<ApiResponse<ChatResponseDto>> askWithUserApiTools(
            @RequestParam String prompt,
            @RequestParam String conversationId,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        Map<String, Object> context = new HashMap<>();
        if (userId != null) context.put("userId", userId);
        return respond(toolChatService.askWithUserApiTools(prompt, conversationId, context));
    }

    @GetMapping("/all")
    @Operation(summary = "Ask question using All Registered Tools")
    public ResponseEntity<ApiResponse<ChatResponseDto>> askWithAllTools(
            @RequestParam String prompt,
            @RequestParam String conversationId,
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestHeader(value = "X-Time-Zone", required = false) String timeZone,
            @RequestHeader(value = "X-Decimal-Precision", required = false) Integer precision) {
        Map<String, Object> context = new HashMap<>();
        if (userId != null) context.put("userId", userId);
        if (timeZone != null && !timeZone.isBlank()) context.put("timeZone", timeZone);
        if (precision != null) context.put("decimalPrecision", precision);
        return respond(toolChatService.askWithAllTools(prompt, conversationId, context));
    }

    private ResponseEntity<ApiResponse<ChatResponseDto>> respond(ChatResponseDto result) {
        return result != null
                ? ResponseEntity.ok(ApiResponse.success(result))
                : ResponseEntity.internalServerError().body(ApiResponse.error("Failed to get response from AI model"));
    }
}
