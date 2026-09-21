package com.learning.chat.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.learning.chat.dto.ChatResponseDto;
import com.learning.chat.dto.DynamicPromptRequest;
import com.learning.chat.service.ChatService;
import com.learning.chat.service.StreamingChatService;
import com.learning.common.dto.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Tag(name = "Chat API", description = "Core Chat endpoints with Mongo chat memory, dynamic prompts, and SSE streaming")
public class ChatController {

    private final ChatService chatService;
    private final StreamingChatService streamingChatService;

    @GetMapping({"", "/ask"})
    @Operation(summary = "Ask question with Chat Memory", description = "Sends a prompt to the OpenAI chat model maintaining conversational memory stored in MongoDB using the provided conversationId.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successful AI completion response"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Missing or invalid parameters"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Failed to communicate with AI model")
    })
    public ResponseEntity<ApiResponse<ChatResponseDto>> ask(
            @Parameter(description = "Prompt or question for the AI model", example = "Explain polymorphism in Java")
            @RequestParam String prompt,
            @Parameter(description = "Conversation ID for session memory retention", example = "session-101")
            @RequestParam String conversationId) {
        return respond(chatService.ask(prompt, conversationId));
    }

    @PostMapping("/dynamic")
    @Operation(summary = "Ask using Dynamic Prompt Template", description = "Generates a prompt dynamically based on structured parameters (topic, persona, tone, language, format, instructions) and queries the AI model.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successful dynamic prompt response"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ApiResponse<ChatResponseDto>> askDynamic(
            @RequestBody DynamicPromptRequest request,
            @Parameter(description = "Conversation ID for session memory retention", example = "session-101")
            @RequestParam String conversationId) {
        return respond(chatService.askDynamic(request, conversationId));
    }

    @GetMapping(value = "/streaming", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Stream Chat Responses (SSE)", description = "Streams tokens in real-time as Server-Sent Events (SSE) from the AI model.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Real-time token stream"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Missing prompt or conversationId")
    })
    public Flux<ChatResponseDto> streaming(
            @Parameter(description = "Prompt or question for streaming completion", example = "Write a poem about clean code architecture")
            @RequestParam String prompt,
            @Parameter(description = "Conversation ID for session memory retention", example = "session-101")
            @RequestParam String conversationId) {
        return streamingChatService.stream(prompt, conversationId);
    }

    private ResponseEntity<ApiResponse<ChatResponseDto>> respond(ChatResponseDto result) {
        return result != null
                ? ResponseEntity.ok(ApiResponse.success(result))
                : ResponseEntity.internalServerError()
                        .body(ApiResponse.error("Failed to get response from AI model"));
    }
}
