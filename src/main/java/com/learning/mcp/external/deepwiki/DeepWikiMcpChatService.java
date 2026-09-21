package com.learning.mcp.external.deepwiki;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Service;

import com.learning.chat.dto.ChatResponseDto;
import com.learning.chat.mapper.ChatResponseMapper;
import com.learning.mcp.external.filter.McpToolValidationFilter;
import com.learning.validation.RequestValidator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeepWikiMcpChatService {

    private final ChatClient chatClient;
    private final SyncMcpToolCallbackProvider syncMcpToolCallbackProvider;
    private final McpToolValidationFilter mcpToolValidationFilter;
    private final ChatResponseMapper chatResponseMapper;
    private final RequestValidator requestValidator;

    public ChatResponseDto chat(String prompt, String conversationId) {
        return chat(prompt, conversationId, null);
    }

    public ChatResponseDto chat(String prompt, String conversationId, List<String> customAllowedTools) {
        requestValidator.validateChatRequest(prompt, conversationId);
        log.info("DeepWiki MCP Client: Processing prompt '{}' for conversation: {}", prompt, conversationId);

        List<ToolCallback> approvedTools = getApprovedTools(customAllowedTools);

        var requestSpec = chatClient.prompt()
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, conversationId))
                .user(prompt);

        if (!approvedTools.isEmpty()) {
            requestSpec.tools(approvedTools.toArray());
        } else {
            log.warn("Proceeding with AI completion without MCP tools (none approved or allow-list empty).");
        }

        ChatResponse response = requestSpec.call().chatResponse();
        return chatResponseMapper.map(response);
    }

    public Flux<String> stream(String prompt, String conversationId) {
        return stream(prompt, conversationId, null);
    }

public Flux<String> stream(String prompt, String conversationId, List<String> customAllowedTools) {
        requestValidator.validateChatRequest(prompt, conversationId);
        log.info("DeepWiki MCP Client: Processing streaming prompt '{}' for conversation: {}", prompt, conversationId);

        List<ToolCallback> approvedTools = getApprovedTools(customAllowedTools);

        var requestSpec = chatClient.prompt()
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, conversationId))
                .user(prompt);

        if (!approvedTools.isEmpty()) {
            requestSpec.tools(approvedTools.toArray());
        } else {
            log.warn("Proceeding with AI streaming without MCP tools (none approved or allow-list empty).");
        }

        return requestSpec.stream().content();
    }

    public List<Map<String, Object>> listTools() {
        List<Map<String, Object>> tools = new ArrayList<>();
        ToolCallback[] callbacks = syncMcpToolCallbackProvider.getToolCallbacks();

        if (callbacks != null) {
            Arrays.stream(callbacks)
                    .filter(cb -> isDeepWikiTool(cb.getToolDefinition().name()))
                    .forEach(cb -> {
                        String toolName = cb.getToolDefinition().name();
                        Map<String, Object> toolInfo = new LinkedHashMap<>();
                        toolInfo.put("server", "DeepWiki MCP (Streamable HTTP)");
                        toolInfo.put("name", toolName);
                        toolInfo.put("description", cb.getToolDefinition().description());
                        toolInfo.put("inputSchema", cb.getToolDefinition().inputSchema());
                        toolInfo.put("approved", mcpToolValidationFilter.getAllowedTools().contains(toolName));
                        tools.add(toolInfo);
                    });
        }
        return tools;
    }

    private List<ToolCallback> getApprovedTools(List<String> customAllowedTools) {
        ToolCallback[] discovered = syncMcpToolCallbackProvider.getToolCallbacks();
        if (discovered == null || discovered.length == 0) {
            return List.of();
        }

        ToolCallback[] deepWikiCallbacks = Arrays.stream(discovered)
                .filter(cb -> isDeepWikiTool(cb.getToolDefinition().name()))
                .toArray(ToolCallback[]::new);

        if (customAllowedTools != null && !customAllowedTools.isEmpty()) {
            return mcpToolValidationFilter.filter(deepWikiCallbacks, customAllowedTools);
        }
        return mcpToolValidationFilter.filter(deepWikiCallbacks);
    }

    private boolean isDeepWikiTool(String toolName) {
        if (toolName == null) {
            return false;
        }
        String lower = toolName.toLowerCase();
        return lower.contains("wiki") || lower.equals("ask_question") || lower.startsWith("devin_");
    }
}
