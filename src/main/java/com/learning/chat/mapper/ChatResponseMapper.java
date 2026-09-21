package com.learning.chat.mapper;

import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Component;

import com.learning.chat.dto.ChatResponseDto;

@Component
public class ChatResponseMapper {

    public ChatResponseDto map(ChatResponse response) {
        if (response == null || response.getResult() == null) {
            return null;
        }

        Usage usage = response.getMetadata() != null ? response.getMetadata().getUsage() : null;

        return ChatResponseDto.builder()
                .content(response.getResult().getOutput() != null
                        ? response.getResult().getOutput().getText()
                        : "")
                .promptTokens(usage != null ? usage.getPromptTokens() : 0)
                .completionTokens(usage != null ? usage.getCompletionTokens() : 0)
                .totalTokens(usage != null ? usage.getTotalTokens() : 0)
                .build();
    }
}
