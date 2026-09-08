package com.learning.tools.api;

import java.util.List;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.learning.tools.model.UserDto;

import lombok.extern.slf4j.Slf4j;

/**
 * API Tool for interacting with the JSONPlaceholder Users REST API.
 * Demonstrates Pure Context-Only Tools (no LLM parameters) and standard tools.
 */
@Slf4j
@Component
public class UserApiTools {

    private final RestClient restClient;

    public UserApiTools(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder
                .baseUrl("https://jsonplaceholder.typicode.com")
                .build();
    }

    /**
     * Pure Context-Only Tool: Requires NO parameters from the LLM.
     * Extracts the authenticated user ID directly from ToolContext.
     */
    @Tool(description = "Fetch the profile details of the current authenticated / logged-in user.")
    public UserDto getCurrentUserProfile(ToolContext toolContext) {
        if (toolContext == null || !toolContext.getContext().containsKey("userId")) {
            throw new IllegalStateException("No userId found in ToolContext");
        }
        Long userId = Long.valueOf(toolContext.getContext().get("userId").toString());
        log.info("UserApiTools.getCurrentUserProfile invoked for user: {}", userId);
        return fetchUserById(userId);
    }

    /**
     * Standard Tool: Takes user ID parameter from the LLM.
     */
    @Tool(description = "Fetch detailed user profile information for a specific user ID.")
    public UserDto getUserById(
            @ToolParam(description = "The unique user ID (e.g., 1, 2, 3)") Long id) {
        log.info("UserApiTools.getUserById: {}", id);
        return fetchUserById(id);
    }

    /**
     * Pure Context-Only Tool: Fetches company details of the current user from context.
     */
    @Tool(description = "Fetch the company details of the current logged-in user.")
    public UserDto.CompanyDto getCurrentUserCompany(ToolContext toolContext) {
        UserDto user = getCurrentUserProfile(toolContext);
        return user != null ? user.company() : null;
    }

    @Tool(description = "Fetch all registered users from the JSONPlaceholder API.")
    public List<UserDto> getAllUsers() {
        return restClient.get()
                .uri("/users")
                .retrieve()
                .body(new ParameterizedTypeReference<List<UserDto>>() {});
    }

    @Tool(description = "Search users from the JSONPlaceholder API by matching name, username, or email.")
    public List<UserDto> searchUsers(
            @ToolParam(description = "Search query to match against name, username, or email") String query) {
        List<UserDto> allUsers = getAllUsers();
        if (allUsers == null || query == null || query.isBlank()) {
            return allUsers;
        }
        String lower = query.trim().toLowerCase();
        return allUsers.stream()
                .filter(u -> (u.name() != null && u.name().toLowerCase().contains(lower))
                        || (u.username() != null && u.username().toLowerCase().contains(lower))
                        || (u.email() != null && u.email().toLowerCase().contains(lower)))
                .toList();
    }

    @Tool(description = "Fetch company details of a specific user by user ID.")
    public UserDto.CompanyDto getUserCompany(
            @ToolParam(description = "The unique user ID") Long id) {
        UserDto user = getUserById(id);
        return user != null ? user.company() : null;
    }

    private UserDto fetchUserById(Long id) {
        return restClient.get()
                .uri("/users/{id}", id)
                .retrieve()
                .body(UserDto.class);
    }
}
