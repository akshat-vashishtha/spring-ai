package com.learning.mcp.internal.client.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.learning.common.dto.ApiResponse;
import com.learning.mcp.internal.client.elicitation.ElicitationSessionStore;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST Controller exposing human-in-the-loop callback endpoints for MCP Elicitation.
 * Allows users, web frontends, or webhooks to review and confirm/decline pending destructive actions.
 */
@Slf4j
@RestController
@RequestMapping("/api/mcp/internal/elicitation")
@RequiredArgsConstructor
@Tag(name = "Internal MCP Elicitation Callback API", description = "Endpoints for Human-in-the-Loop review and confirmation of pending MCP tool actions")
public class ElicitationCallbackController {

    private final ElicitationSessionStore sessionStore;

    @GetMapping("/pending")
    @Operation(summary = "List pending elicitation confirmation requests", description = "Retrieves all currently active actions awaiting user confirmation.")
    public ResponseEntity<ApiResponse<Map<String, String>>> getPendingRequests() {
        Map<String, String> pending = sessionStore.listPendingSessions();
        return ResponseEntity.ok(ApiResponse.success(pending));
    }

    @PostMapping("/confirm/{confirmationId}")
    @Operation(summary = "Confirm or decline a pending MCP elicitation action", description = "Resolves the pending confirmation session by submitting the user's approval decision.")
    public ResponseEntity<ApiResponse<String>> confirmAction(
            @Parameter(description = "Confirmation ID from the pending elicitation request", example = "a1b2c3d4")
            @PathVariable String confirmationId,
            @Parameter(description = "Approval decision (true = confirm/delete, false = cancel/decline)", example = "true")
            @RequestParam boolean approved) {

        log.info("ElicitationCallbackController: Received decision for confirmationId='{}', approved={}",
                confirmationId, approved);

        boolean resolved = sessionStore.resolveSession(confirmationId, approved);
        if (!resolved) {
            log.warn("ElicitationCallbackController: Failed to resolve. Invalid or expired confirmationId: '{}'", confirmationId);
            return ResponseEntity.badRequest().body(ApiResponse.error(
                    "Invalid, expired, or already resolved confirmation ID: " + confirmationId
            ));
        }

        String statusMessage = approved
                ? "Action APPROVED successfully for confirmation ID: " + confirmationId
                : "Action DECLINED successfully for confirmation ID: " + confirmationId;

        return ResponseEntity.ok(ApiResponse.success(statusMessage));
    }
}
