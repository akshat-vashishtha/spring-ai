package com.learning.mcp.internal.client.sampling;

import java.util.stream.Collectors;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.mcp.annotation.McpSampling;
import org.springframework.ai.mcp.customizer.McpClientCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.extern.slf4j.Slf4j;

/**
 * Client-side MCP Sampling Handler for Internal Product Catalog.
 * Intercepts sampling/createMessage requests sent by the MCP Server and delegates
 * the generation to the client's ChatClient (OpenAI LLM).
 */
@Slf4j
@Component
public class ProductMcpSamplingHandler implements McpClientCustomizer<McpClient.SyncSpec> {

    private final ChatClient chatClient;
    private final String modelName;

    public ProductMcpSamplingHandler(
            @Lazy ChatClient chatClient,
            @Value("${spring.ai.openai.chat.options.model}") String modelName) {
        this.chatClient = chatClient;
        this.modelName = modelName;
    }

    @Override
    public void customize(String clientName, McpClient.SyncSpec spec) {
        if ("product-catalog".equals(clientName)) {
            log.info("Configuring MCP Client '{}' with sampling capability and handler", clientName);
            spec.capabilities(McpSchema.ClientCapabilities.builder()
                    .sampling()
                    .elicitation()
                    .build());
            spec.sampling(this::handleSampling);
        }
    }

    /**
     * Handles sampling requests from the MCP Server for 'product-catalog' only.
     * Takes the incoming CreateMessageRequest from the server, invokes the client LLM,
     * and returns the generated content wrapped in a CreateMessageResult.
     */
    @McpSampling(clients = "product-catalog")
    public McpSchema.CreateMessageResult handleSampling(McpSchema.CreateMessageRequest request) {
        log.info("Client @McpSampling: Received sampling request from MCP server. Model: '{}', System prompt: '{}', Messages: {}",
                modelName,
                request.systemPrompt(),
                request.messages() != null ? request.messages().size() : 0);

        String userPrompt = "";
        if (request.messages() != null) {
            userPrompt = request.messages().stream()
                    .filter(msg -> msg.content() instanceof McpSchema.TextContent)
                    .map(msg -> ((McpSchema.TextContent) msg.content()).text())
                    .collect(Collectors.joining("\n\n"));
        }

        var promptSpec = chatClient.prompt().user(userPrompt);
        if (request.systemPrompt() != null && !request.systemPrompt().isBlank()) {
            promptSpec = promptSpec.system(request.systemPrompt());
        }

        String generatedResponse = promptSpec.call().content();

        log.info("Client @McpSampling: Generated response successfully (length: {})",
                generatedResponse != null ? generatedResponse.length() : 0);

        return McpSchema.CreateMessageResult.builder(
                McpSchema.Role.ASSISTANT,
                McpSchema.TextContent.builder(generatedResponse != null ? generatedResponse : "").build(),
                modelName
        ).build();
    }
}
