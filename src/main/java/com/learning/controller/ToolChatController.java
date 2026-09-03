package com.learning.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.learning.dto.ApiResponse;
import com.learning.dto.ChatResponseDto;
import com.learning.service.ToolChatService;

import lombok.RequiredArgsConstructor;

/**
 * REST Controller exposing endpoints demonstrating Spring AI Tool Calling.
 */
@RestController
@RequestMapping("/api/chat/tools")
@RequiredArgsConstructor
public class ToolChatController {

    private final ToolChatService toolChatService;

    /**
     * Ask question with Normal Tools enabled (DateTime, Calculator).
     * Example: /api/chat/tools/normal?prompt=What is current time in UTC and what is 15 percent of 850?&conversationId=c1
     */
    @GetMapping("/normal")
    public ResponseEntity<ApiResponse<ChatResponseDto>> askWithNormalTools(
            @RequestParam String prompt,
            @RequestParam String conversationId) {
        return respond(toolChatService.askWithNormalTools(prompt, conversationId));
    }

    /**
     * Ask question with JSONPlaceholder User API Tools enabled.
     * Example: /api/chat/tools/users?prompt=Give me user details for user with id 1&conversationId=c1
     */
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<ChatResponseDto>> askWithUserApiTools(
            @RequestParam String prompt,
            @RequestParam String conversationId) {
        return respond(toolChatService.askWithUserApiTools(prompt, conversationId));
    }

    /**
     * Ask question with All Tools enabled (Normal Tools + JSONPlaceholder API).
     * Example: /api/chat/tools/all?prompt=Who is user 2 and convert their city temperature 25 Celsius to Fahrenheit?&conversationId=c1
     */
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<ChatResponseDto>> askWithAllTools(
            @RequestParam String prompt,
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
