package com.learning.mcp.external.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.ai.mcp.McpConnectionInfo;
import org.springframework.ai.mcp.McpToolFilter;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import io.modelcontextprotocol.spec.McpSchema;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.mcp.tools")
public class McpToolValidationFilter implements McpToolFilter {

    private List<String> allowed = new ArrayList<>();

    @Override
    public boolean test(McpConnectionInfo connectionInfo, McpSchema.Tool tool) {
        if (connectionInfo != null && connectionInfo.clientInfo() != null) {
            String clientName = connectionInfo.clientInfo().name();
            if (clientName != null && clientName.toLowerCase().contains("drawio")) {
                return true;
            }
        }
        String toolName = tool.name();
        if (allowed.contains(toolName)) {
            log.info("MCP Tool Filter: Kept tool '{}'", toolName);
            return true;
        } else {
            log.info("MCP Tool Filter: Dropped tool '{}' (not in allow-list)", toolName);
            return false;
        }
    }

    public List<ToolCallback> filter(ToolCallback[] discoveredCallbacks) {
        return filter(discoveredCallbacks, this.allowed);
    }

    public List<ToolCallback> filter(List<ToolCallback> discoveredCallbacks) {
        return filter(discoveredCallbacks, this.allowed);
    }

    public List<ToolCallback> filter(ToolCallback[] discoveredCallbacks, Collection<String> allowedTools) {
        if (discoveredCallbacks == null || discoveredCallbacks.length == 0) {
            log.warn("MCP Tool Filter: Discovered tool callbacks list is null or empty.");
            return Collections.emptyList();
        }
        return filter(Arrays.asList(discoveredCallbacks), allowedTools);
    }

    public List<ToolCallback> filter(List<ToolCallback> discoveredCallbacks, Collection<String> allowedTools) {
        if (allowedTools == null || allowedTools.isEmpty()) {
            log.warn("MCP Tool Filter: Allow-list is empty. Proceeding without tools.");
            return Collections.emptyList();
        }
        if (discoveredCallbacks == null || discoveredCallbacks.isEmpty()) {
            log.warn("MCP Tool Filter: Discovered tool callbacks list is null or empty.");
            return Collections.emptyList();
        }

        Set<String> allowSet = new HashSet<>(allowedTools);
        List<ToolCallback> approved = new ArrayList<>();
        for (ToolCallback callback : discoveredCallbacks) {
            String toolName = callback.getToolDefinition().name();
            if (allowSet.contains(toolName)) {
                log.info("MCP Tool Filter: Kept tool '{}'", toolName);
                approved.add(callback);
            } else {
                log.info("MCP Tool Filter: Dropped tool '{}' (not in allow-list)", toolName);
            }
        }

        if (approved.isEmpty()) {
            log.warn("MCP Tool Filter: No tools matched the allow-list {}. Proceeding without tools.", allowedTools);
        }
        return approved;
    }

    public Set<String> getAllowedTools() {
        return new HashSet<>(allowed);
    }
}
