package com.learning.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SafeGuardAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.learning.advisor.ExecutionAuditAdvisor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class AiConfig {

    private final AiProperties aiProperties;

    @Bean
    public SimpleLoggerAdvisor simpleLoggerAdvisor() {
        return SimpleLoggerAdvisor.builder().build();
    }

    @Bean
    public SafeGuardAdvisor safeGuardAdvisor() {
        return SafeGuardAdvisor.builder()
                .sensitiveWords(aiProperties.getSafeguard().getSensitiveWords())
                .failureResponse(aiProperties.getSafeguard().getFailureResponse())
                .build();
    }

    @Bean
    public MessageChatMemoryAdvisor messageChatMemoryAdvisor(ChatMemory chatMemory) {
        return MessageChatMemoryAdvisor.builder(chatMemory).build();
    }

    @Bean
    public ChatClient chatClient(ChatModel chatModel,
                                 MessageChatMemoryAdvisor messageChatMemoryAdvisor,
                                 SimpleLoggerAdvisor simpleLoggerAdvisor,
                                 SafeGuardAdvisor safeGuardAdvisor,
                                 ExecutionAuditAdvisor executionAuditAdvisor) {
        log.info("Configuring ChatClient with MessageChatMemoryAdvisor, SafeGuardAdvisor ({} sensitive words), SimpleLoggerAdvisor, and ExecutionAuditAdvisor",
                aiProperties.getSafeguard().getSensitiveWords().size());

        return ChatClient.builder(chatModel)
                .defaultAdvisors(
                        messageChatMemoryAdvisor,
                        simpleLoggerAdvisor,
                        safeGuardAdvisor,
                        executionAuditAdvisor)
                .build();
    }
}