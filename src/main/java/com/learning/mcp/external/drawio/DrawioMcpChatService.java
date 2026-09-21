package com.learning.mcp.external.drawio;

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

@Slf4j
@Service
@RequiredArgsConstructor
public class DrawioMcpChatService {

    private final ChatClient chatClient;
    private final SyncMcpToolCallbackProvider syncMcpToolCallbackProvider;
    private final ChatResponseMapper chatResponseMapper;
    private final RequestValidator requestValidator;

    public ChatResponseDto generateDiagram(String prompt, String conversationId) {
        requestValidator.validateChatRequest(prompt, conversationId);
        log.info("Draw.io MCP Client: Processing prompt '{}' for conversation: {}", prompt, conversationId);

        List<ToolCallback> drawioTools = getDrawioToolCallbacks();

        var requestSpec = chatClient.prompt()
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, conversationId))
                .user(prompt);

        if (!drawioTools.isEmpty()) {
            requestSpec.tools(drawioTools.toArray());
        } else {
            log.warn("Draw.io tools not found; proceeding without tools.");
        }

        ChatResponse response = requestSpec.call().chatResponse();
        return chatResponseMapper.map(response);
    }

    public List<Map<String, Object>> listDrawioTools() {
        List<Map<String, Object>> tools = new ArrayList<>();
        List<ToolCallback> callbacks = getDrawioToolCallbacks();

        for (ToolCallback cb : callbacks) {
            Map<String, Object> toolInfo = new LinkedHashMap<>();
            toolInfo.put("server", "Draw.io (@drawio/mcp)");
            toolInfo.put("name", cb.getToolDefinition().name());
            toolInfo.put("description", cb.getToolDefinition().description());
            toolInfo.put("inputSchema", cb.getToolDefinition().inputSchema());
            tools.add(toolInfo);
        }
        return tools;
    }

    private List<ToolCallback> getDrawioToolCallbacks() {
        ToolCallback[] allCallbacks = syncMcpToolCallbackProvider.getToolCallbacks();
        if (allCallbacks == null || allCallbacks.length == 0) {
            return List.of();
        }
        return Arrays.stream(allCallbacks)
                .filter(cb -> {
                    String name = cb.getToolDefinition().name().toLowerCase();
                    return name.contains("drawio") || name.contains("diagram");
                })
                .toList();
    }
}
