package com.learning.tools.api;

import java.util.Arrays;
import java.util.List;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.learning.tools.model.UserDto;

import lombok.extern.slf4j.Slf4j;

/**
 * API Tool for interacting with the JSONPlaceholder Users REST API (https://jsonplaceholder.typicode.com/users).
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

    @Tool(description = "Fetch detailed user profile information (name, email, phone, website, company, address) from JSONPlaceholder API by user ID.")
    public UserDto getUserById(
            @ToolParam(description = "The unique user ID (e.g., 1, 2, 3, up to 10)") Long id) {
        log.info("UserApiTools.getUserById invoked for ID: {}", id);
        try {
            UserDto user = restClient.get()
                    .uri("/users/{id}", id)
                    .retrieve()
                    .body(UserDto.class);
            log.info("Successfully fetched user with ID {}: {}", id, user != null ? user.name() : "null");
            return user;
        } catch (Exception e) {
            log.error("Failed to fetch user with ID: {}", id, e);
            throw new RuntimeException("Could not retrieve user with ID " + id + ": " + e.getMessage(), e);
        }
    }

    @Tool(description = "Fetch all registered users from the JSONPlaceholder API.")
    public List<UserDto> getAllUsers() {
        log.info("UserApiTools.getAllUsers invoked");
        try {
            List<UserDto> users = restClient.get()
                    .uri("/users")
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<UserDto>>() {});
            log.info("Successfully retrieved {} users", users != null ? users.size() : 0);
            return users;
        } catch (Exception e) {
            log.error("Failed to fetch all users", e);
            throw new RuntimeException("Could not retrieve users list: " + e.getMessage(), e);
        }
    }

    @Tool(description = "Search users from the JSONPlaceholder API by matching name, username, or email.")
    public List<UserDto> searchUsers(
            @ToolParam(description = "Search query to match against user name, username, or email (case-insensitive)") String query) {
        log.info("UserApiTools.searchUsers invoked with query: '{}'", query);
        try {
            List<UserDto> allUsers = getAllUsers();
            if (allUsers == null || query == null || query.isBlank()) {
                return allUsers;
            }
            String lowerQuery = query.trim().toLowerCase();
            List<UserDto> matched = allUsers.stream()
                    .filter(u -> (u.name() != null && u.name().toLowerCase().contains(lowerQuery))
                            || (u.username() != null && u.username().toLowerCase().contains(lowerQuery))
                            || (u.email() != null && u.email().toLowerCase().contains(lowerQuery)))
                    .toList();
            log.info("Search for '{}' returned {} results", query, matched.size());
            return matched;
        } catch (Exception e) {
            log.error("Error searching users with query: '{}'", query, e);
            throw new RuntimeException("Could not search users: " + e.getMessage(), e);
        }
    }

    @Tool(description = "Fetch the company details of a specific user by user ID from JSONPlaceholder API.")
    public UserDto.CompanyDto getUserCompany(
            @ToolParam(description = "The unique user ID") Long id) {
        log.info("UserApiTools.getUserCompany invoked for user ID: {}", id);
        UserDto user = getUserById(id);
        return user != null ? user.company() : null;
    }
}
