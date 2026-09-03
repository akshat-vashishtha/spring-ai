package com.learning.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;

import com.learning.dto.ChatResponseDto;
import com.learning.dto.DynamicPromptRequest;
import com.learning.mapper.ChatResponseMapper;
import com.learning.prompt.DynamicPromptFactory;
import com.learning.validation.RequestValidator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@Slf4j
@Service
@RequiredArgsConstructor
public class StreamingChatService {

    private final ChatClient chatClient;
    private final DynamicPromptFactory dynamicPromptFactory;
    private final ChatResponseMapper chatResponseMapper;
    private final RequestValidator requestValidator;

    public Flux<ChatResponseDto> stream(String prompt, String conversationId) {
        requestValidator.validateChatRequest(prompt, conversationId);
        log.info("Processing streaming prompt for conversation: {}", conversationId);
        return chatClient.prompt()
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, conversationId))
                .user(prompt)
                .stream()
                .chatResponse()
                .map(chatResponseMapper::map);
    }

    public Flux<ChatResponseDto> streamDynamic(DynamicPromptRequest request, String conversationId) {
        requestValidator.validateDynamicPrompt(request, conversationId);
        log.info("Processing dynamic streaming prompt for conversation: {}", conversationId);

        return chatClient.prompt()
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, conversationId))
                .messages(dynamicPromptFactory.create(request))
                .stream()
                .chatResponse()
                .map(chatResponseMapper::map);
    }
}


