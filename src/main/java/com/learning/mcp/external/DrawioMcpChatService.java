package com.learning.mcp.external;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Service;

import com.learning.chat.dto.ChatResponseDto;
import com.learning.chat.mapper.ChatResponseMapper;
import com.learning.validation.RequestValidator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * External MCP Client Service connected to the official Draw.io MCP Server (@drawio/mcp).
 * Allows the AI model to generate, open, and edit Draw.io diagrams via MCP tool calling.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DrawioMcpChatService {

    private final ChatClient chatClient;
    private final SyncMcpToolCallbackProvider syncMcpToolCallbackProvider;
    private final ChatResponseMapper chatResponseMapper;
    private final RequestValidator requestValidator;

    /**
     * Send prompt to AI equipped with official Draw.io MCP tools to generate or edit diagrams.
     */
    public ChatResponseDto generateDiagram(String prompt, String conversationId) {
        requestValidator.validateChatRequest(prompt, conversationId);
        log.info("External Draw.io MCP Client: Processing prompt '{}' for conversation: {}", prompt, conversationId);

        ChatResponse response = chatClient.prompt()
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, conversationId))
                .tools(syncMcpToolCallbackProvider)
                .user(prompt)
                .call()
                .chatResponse();

        return chatResponseMapper.map(response);
    }

    /**
     * List all tools discovered from the connected Draw.io MCP server.
     */
    public List<Map<String, Object>> listDrawioTools() {
        List<Map<String, Object>> tools = new ArrayList<>();
        ToolCallback[] callbacks = syncMcpToolCallbackProvider.getToolCallbacks();

        if (callbacks != null) {
            Arrays.stream(callbacks).forEach(cb -> {
                Map<String, Object> toolInfo = new LinkedHashMap<>();
                toolInfo.put("server", "Draw.io (@drawio/mcp)");
                toolInfo.put("name", cb.getToolDefinition().name());
                toolInfo.put("description", cb.getToolDefinition().description());
                toolInfo.put("inputSchema", cb.getToolDefinition().inputSchema());
                tools.add(toolInfo);
            });
        }
        return tools;
    }
}
