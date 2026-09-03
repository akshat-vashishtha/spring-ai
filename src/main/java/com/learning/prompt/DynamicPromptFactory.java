package com.learning.prompt;

import java.util.List;
import java.util.Map;

import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import com.learning.dto.DynamicPromptRequest;

@Component
public class DynamicPromptFactory {

    private final Resource systemPromptResource;
    private final Resource userPromptResource;

    public DynamicPromptFactory(
            @Value("classpath:prompts/system-prompt.st") Resource systemPromptResource,
            @Value("classpath:prompts/user-prompt.st") Resource userPromptResource) {
        this.systemPromptResource = systemPromptResource;
        this.userPromptResource = userPromptResource;
    }

    public List<Message> create(DynamicPromptRequest request) {
        Message systemMessage = new SystemPromptTemplate(systemPromptResource)
                .createMessage(Map.of(
                        "persona", request.getPersona(),
                        "tone", request.getTone(),
                        "language", request.getLanguage()));

        Message userMessage = new PromptTemplate(userPromptResource)
                .createMessage(Map.of(
                        "topic", request.getTopic(),
                        "format", request.getFormat(),
                        "additionalInstructions", request.getAdditionalInstructions()));

        return List.of(systemMessage, userMessage);
    }
}
