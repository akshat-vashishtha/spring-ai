package com.learning.mcp.internal.client.elicitation;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Thread-safe Store managing pending MCP Elicitation confirmation sessions.
 * Holds asynchronous CompletableFuture instances until resolved via REST Callback API.
 */
@Slf4j
@Component
public class ElicitationSessionStore {

    public record PendingSession(
            String confirmationId,
            String message,
            long createdAtMillis,
            CompletableFuture<Boolean> future
    ) {}

    private final Map<String, PendingSession> pendingSessions = new ConcurrentHashMap<>();

    /**
     * Creates and registers a new pending elicitation session.
     */
    public PendingSession createSession(String message) {
        String confirmationId = UUID.randomUUID().toString().substring(0, 8);
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        PendingSession session = new PendingSession(confirmationId, message, System.currentTimeMillis(), future);

        pendingSessions.put(confirmationId, session);
        log.info("ElicitationSessionStore: Created pending session [id: '{}'] for message: '{}'", confirmationId, message);
        return session;
    }

    /**
     * Resolves a pending session with user's decision (true=approved, false=declined).
     *
     * @return true if session was found and resolved, false otherwise
     */
    public boolean resolveSession(String confirmationId, boolean approved) {
        PendingSession session = pendingSessions.remove(confirmationId);
        if (session != null) {
            log.info("ElicitationSessionStore: Resolving session '{}' with approved={}", confirmationId, approved);
            return session.future().complete(approved);
        }
        log.warn("ElicitationSessionStore: Attempted to resolve non-existent or expired session '{}'", confirmationId);
        return false;
    }

    /**
     * Returns an unmodifiable snapshot map of all currently active pending sessions (id -> message).
     */
    public Map<String, String> listPendingSessions() {
        return Collections.unmodifiableMap(
                pendingSessions.values().stream()
                        .collect(Collectors.toMap(PendingSession::confirmationId, PendingSession::message))
        );
    }

    /**
     * Cleans up a session on timeout or cancellation.
     */
    public void removeSession(String confirmationId) {
        pendingSessions.remove(confirmationId);
    }
}
