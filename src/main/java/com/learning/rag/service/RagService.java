package com.learning.rag.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.stereotype.Service;

import com.learning.chat.dto.ChatResponseDto;
import com.learning.chat.mapper.ChatResponseMapper;
import com.learning.validation.RequestValidator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RagService {

    private final ChatClient chatClient;
    private final QuestionAnswerAdvisor questionAnswerAdvisor;
    private final RetrievalAugmentationAdvisor retrievalAugmentationAdvisor;
    private final ChatResponseMapper chatResponseMapper;
    private final RequestValidator requestValidator;

    /**
     * Basic RAG: Augments user prompt with context retrieved from VectorStore.
     */
    public ChatResponseDto askWithKnowledge(String prompt, String conversationId) {
        requestValidator.validateChatRequest(prompt, conversationId);
        log.info("Processing basic RAG for conversation: {}", conversationId);
        ChatResponse response = chatClient.prompt()
                .advisors(questionAnswerAdvisor)
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, conversationId))
                .user(prompt)
                .call()
                .chatResponse();

        return chatResponseMapper.map(response);
    }

    /**
     * Advanced RAG: Query rewritten via LLM before retrieval with contextual augmentation.
     */
    public ChatResponseDto askWithKnowledgeAdvanced(String prompt, String conversationId) {
        requestValidator.validateChatRequest(prompt, conversationId);
        log.info("Processing advanced contextual RAG for conversation: {}", conversationId);
        ChatResponse response = chatClient.prompt()
                .advisors(retrievalAugmentationAdvisor)
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, conversationId))
                .user(prompt)
                .call()
                .chatResponse();

        return chatResponseMapper.map(response);
    }
}
