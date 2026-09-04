package com.learning.mcp.internal.client;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Service;

import com.learning.dto.ChatResponseDto;
import com.learning.mapper.ChatResponseMapper;
import com.learning.mcp.internal.server.ProductMcpTools;
import com.learning.validation.RequestValidator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Internal MCP Client Service that utilizes Spring AI ChatClient to execute prompts
 * by invoking the Product Catalog MCP Server Tools.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductMcpChatService {

    private final ChatClient chatClient;
    private final ProductMcpTools productMcpTools;
    private final ChatResponseMapper chatResponseMapper;
    private final RequestValidator requestValidator;

    /**
     * Chat with AI using Product Catalog MCP Tools.
     */
    public ChatResponseDto chatWithProductTools(String prompt, String conversationId) {
        requestValidator.validateChatRequest(prompt, conversationId);
        log.info("Internal MCP Client: Processing prompt '{}' for conversation: {}", prompt, conversationId);

        ChatResponse response = chatClient.prompt()
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, conversationId))
                .tools(productMcpTools)
                .user(prompt)
                .call()
                .chatResponse();

        return chatResponseMapper.map(response);
    }
}
