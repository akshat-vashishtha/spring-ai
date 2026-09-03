package com.learning.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.learning.dto.ApiResponse;
import com.learning.dto.ChatResponseDto;
import com.learning.dto.DynamicPromptRequest;
import com.learning.service.ChatService;
import com.learning.service.StreamingChatService;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final StreamingChatService streamingChatService;

    @GetMapping({"", "/ask"})
    public ResponseEntity<ApiResponse<ChatResponseDto>> ask(
            @RequestParam String prompt,
            @RequestParam String conversationId) {
        return respond(chatService.ask(prompt, conversationId));
    }

    @GetMapping("/rag")
    public ResponseEntity<ApiResponse<ChatResponseDto>> askWithKnowledge(
            @RequestParam String prompt,
            @RequestParam String conversationId) {
        return respond(chatService.askWithKnowledge(prompt, conversationId));
    }

    @GetMapping("/rag/advance")
    public ResponseEntity<ApiResponse<ChatResponseDto>> askWithKnowledgeAdvanced(
            @RequestParam String prompt,
            @RequestParam String conversationId) {
        return respond(chatService.askWithKnowledgeAdvanced(prompt, conversationId));
    }

    @PostMapping("/dynamic")
    public ResponseEntity<ApiResponse<ChatResponseDto>> askDynamic(
            @RequestBody DynamicPromptRequest request,
            @RequestParam String conversationId) {
        return respond(chatService.askDynamic(request, conversationId));
    }

    @GetMapping(value = "/streaming", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ChatResponseDto> streaming(
            @RequestParam String prompt,
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





