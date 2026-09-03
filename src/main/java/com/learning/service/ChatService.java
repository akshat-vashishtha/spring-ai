package com.learning.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.stereotype.Service;

import com.learning.dto.ChatResponseDto;
import com.learning.dto.DynamicPromptRequest;
import com.learning.mapper.ChatResponseMapper;
import com.learning.prompt.DynamicPromptFactory;
import com.learning.validation.RequestValidator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatClient chatClient;
    private final QuestionAnswerAdvisor questionAnswerAdvisor;
    private final RetrievalAugmentationAdvisor retrievalAugmentationAdvisor;
    private final DynamicPromptFactory dynamicPromptFactory;
    private final ChatResponseMapper chatResponseMapper;
    private final RequestValidator requestValidator;

    /**
     * Standard query.
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
     * Query augmented with relevant documents retrieved from Qdrant.
     */
    public ChatResponseDto askWithKnowledge(String prompt, String conversationId) {
        requestValidator.validateChatRequest(prompt, conversationId);
        log.info("Processing knowledge-augmented ask for conversation: {}", conversationId);
        ChatResponse response = chatClient.prompt()
                .advisors(questionAnswerAdvisor)
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, conversationId))
                .user(prompt)
                .call()
                .chatResponse();

        return chatResponseMapper.map(response);
    }

    /**
     * Advanced RAG: query is rewritten via LLM before retrieval,
     * and empty context is allowed as fallback to LLM knowledge.
     */
    public ChatResponseDto askWithKnowledgeAdvanced(String prompt, String conversationId) {
        requestValidator.validateChatRequest(prompt, conversationId);
        log.info("Processing advanced RAG ask for conversation: {}", conversationId);
        ChatResponse response = chatClient.prompt()
                .advisors(retrievalAugmentationAdvisor)
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

    