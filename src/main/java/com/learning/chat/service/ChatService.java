package com.learning.chat.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Service;

import com.learning.chat.dto.ChatResponseDto;
import com.learning.chat.dto.DynamicPromptRequest;
import com.learning.chat.mapper.ChatResponseMapper;
import com.learning.chat.prompt.DynamicPromptFactory;
import com.learning.validation.RequestValidator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatClient chatClient;
    private final DynamicPromptFactory dynamicPromptFactory;
    private final ChatResponseMapper chatResponseMapper;
    private final RequestValidator requestValidator;

    /**
     * Standard query maintaining conversational memory in MongoDB.
     */
    public ChatResponseDto ask(String prompt, String conversationId) {
        requestValidator.validateChatRequest(prompt, conversationId);
        log.info("Processing ask for conversation: {}", conversationId);
        ChatResponse response = chatClient.prompt()
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, conversationId))
                .user(prompt)
                .call()
                .chatResponse();

        return chatResponseMapper.map(response);
    }

    /**
     * Dynamic query using SystemPromptTemplate & PromptTemplate from files.
     */
    public ChatResponseDto askDynamic(DynamicPromptRequest request, String conversationId) {
        requestValidator.validateDynamicPrompt(request, conversationId);
        log.info("Processing dynamic prompt for conversation: {}", conversationId);

        ChatResponse response = chatClient.prompt()
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, conversationId))
                .messages(dynamicPromptFactory.create(request))
                .call()
                .chatResponse();

        return chatResponseMapper.map(response);
    }
}
