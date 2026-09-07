package com.learning.tools.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Service;

import com.learning.chat.dto.ChatResponseDto;
import com.learning.chat.mapper.ChatResponseMapper;
import com.learning.tools.api.UserApiTools;
import com.learning.tools.normal.CalculatorTools;
import com.learning.tools.normal.DateTimeTools;
import com.learning.validation.RequestValidator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service orchestrating Tool Calling (Function Calling) with Spring AI ChatClient.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ToolChatService {

    private final ChatClient chatClient;
    private final DateTimeTools dateTimeTools;
    private final CalculatorTools calculatorTools;
    private final UserApiTools userApiTools;
    private final ChatResponseMapper chatResponseMapper;
    private final RequestValidator requestValidator;

    /**
     * Execute prompt with Normal / Local Tools (DateTime & Calculator).
     */
    public ChatResponseDto askWithNormalTools(String prompt, String conversationId) {
        requestValidator.validateChatRequest(prompt, conversationId);
        log.info("Processing prompt with Normal Tools for conversation: {}", conversationId);

        ChatResponse response = chatClient.prompt()
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, conversationId))
                .tools(dateTimeTools, calculatorTools)
                .user(prompt)
                .call()
                .chatResponse();

        return chatResponseMapper.map(response);
    }

    /**
     * Execute prompt with External REST API Tools (JSONPlaceholder Users API).
     */
    public ChatResponseDto askWithUserApiTools(String prompt, String conversationId) {
        requestValidator.validateChatRequest(prompt, conversationId);
        log.info("Processing prompt with User API Tools for conversation: {}", conversationId);

        ChatResponse response = chatClient.prompt()
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, conversationId))
                .tools(userApiTools)
                .user(prompt)
                .call()
                .chatResponse();

        return chatResponseMapper.map(response);
    }

    /**
     * Execute prompt with All Available Tools (Normal Tools + API Tools).
     */
    public ChatResponseDto askWithAllTools(String prompt, String conversationId) {
        requestValidator.validateChatRequest(prompt, conversationId);
        log.info("Processing prompt with All Tools for conversation: {}", conversationId);

        ChatResponse response = chatClient.prompt()
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, conversationId))
                .tools(dateTimeTools, calculatorTools, userApiTools)
                .user(prompt)
                .call()
                .chatResponse();

        return chatResponseMapper.map(response);
    }
}
