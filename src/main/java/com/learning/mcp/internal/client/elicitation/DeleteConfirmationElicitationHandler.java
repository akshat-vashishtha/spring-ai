package com.learning.mcp.internal.client.elicitation;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.springframework.ai.mcp.annotation.McpElicitation;
import org.springframework.ai.mcp.customizer.McpClientCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Client-side MCP Elicitation Handler.
 * Receives elicitation requests from the internal 'product-catalog' MCP Server
 * and coordinates user confirmation via REST Callback, Console, or Automated modes.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeleteConfirmationElicitationHandler implements McpClientCustomizer<McpClient.SyncSpec> {

    private final ElicitationSessionStore sessionStore;

    @Value("${app.mcp.elicitation.mode:callback}")
    private String mode;

    @Value("${app.mcp.elicitation.timeout-seconds:60}")
    private long timeoutSeconds;

    @Override
    public void customize(String clientName, McpClient.SyncSpec spec) {
        if ("product-catalog".equals(clientName)) {
            log.info("Configuring MCP Client '{}' with Elicitation capability and handler", clientName);
            spec.capabilities(McpSchema.ClientCapabilities.builder()
                    .sampling()
                    .elicitation()
                    .build());
            spec.elicitation(this::handleElicitation);
        }
    }

    /**
     * Handles MCP Elicitation form requests sent from the 'product-catalog' MCP server.
     *
     * @param request The ElicitFormRequest containing the confirmation message and schema
     * @return ElicitResult containing the user's action and response content
     */
    @McpElicitation(clients = "product-catalog")
    public McpSchema.ElicitResult handleElicitation(McpSchema.ElicitFormRequest request) {
        String message = request != null ? request.message() : "Confirm action?";
        Map<String, Object> requestedSchema = request != null ? request.requestedSchema() : Map.of();

        log.info("Client @McpElicitation received request: message='{}', schema={}", message, requestedSchema);

        String activeMode = mode != null ? mode.trim().toLowerCase() : "callback";

        McpSchema.ElicitResult result;
        switch (activeMode) {
            case "callback" -> {
                result = promptUserViaCallback(message);
            }
            case "console" -> {
                result = promptUserViaConsole(message);
            }
            case "auto-accept" -> {
                log.info("Client Elicitation [auto-accept mode]: Automatically confirming deletion.");
                result = new McpSchema.ElicitResult(
                        McpSchema.ElicitResult.Action.ACCEPT,
                        Map.of("confirm", true)
                );
            }
            case "auto-decline" -> {
                log.info("Client Elicitation [auto-decline mode]: Automatically declining deletion.");
                result = new McpSchema.ElicitResult(
                        McpSchema.ElicitResult.Action.DECLINE,
                        Map.of("confirm", false)
                );
            }
            default -> {
                log.warn("Unknown elicitation mode '{}'. Defaulting to safest behavior (DECLINE).", activeMode);
                result = new McpSchema.ElicitResult(
                        McpSchema.ElicitResult.Action.DECLINE,
                        Map.of("confirm", false)
                );
            }
        }

        log.info("Client @McpElicitation returning result: action={}, content={}", result.action(), result.content());
        return result;
    }

    /**
     * REST Callback Strategy:
     * Registers a pending session in ElicitationSessionStore and waits for an external HTTP POST callback.
     */
    private McpSchema.ElicitResult promptUserViaCallback(String message) {
        ElicitationSessionStore.PendingSession session = sessionStore.createSession(message);
        String confirmationId = session.confirmationId();

        log.info("\n"
                + "=================================================================================\n"
                + "🚨 MCP ELICITATION CONFIRMATION REQUIRED (REST CALLBACK) 🚨\n"
                + "Action Message : {}\n"
                + "Confirmation ID: {}\n"
                + "To Confirm     : POST http://localhost:8080/api/mcp/internal/elicitation/confirm/{}?approved=true\n"
                + "To Decline     : POST http://localhost:8080/api/mcp/internal/elicitation/confirm/{}?approved=false\n"
                + "Timeout        : {} seconds\n"
                + "=================================================================================",
                message, confirmationId, confirmationId, confirmationId, timeoutSeconds);

        try {
            Boolean decision = session.future().get(timeoutSeconds, TimeUnit.SECONDS);

            if (Boolean.TRUE.equals(decision)) {
                log.info("Elicitation Callback approved by user for confirmationId: '{}'", confirmationId);
                return new McpSchema.ElicitResult(
                        McpSchema.ElicitResult.Action.ACCEPT,
                        Map.of("confirm", true)
                );
            } else {
                log.info("Elicitation Callback declined by user for confirmationId: '{}'", confirmationId);
                return new McpSchema.ElicitResult(
                        McpSchema.ElicitResult.Action.DECLINE,
                        Map.of("confirm", false)
                );
            }
        } catch (TimeoutException e) {
            log.warn("Elicitation Callback timed out after {}s for confirmationId: '{}'", timeoutSeconds, confirmationId);
            sessionStore.removeSession(confirmationId);
            return new McpSchema.ElicitResult(
                    McpSchema.ElicitResult.Action.CANCEL,
                    Map.of()
            );
        } catch (Exception e) {
            log.error("Exception while awaiting elicitation callback for confirmationId: '{}'", confirmationId, e);
            sessionStore.removeSession(confirmationId);
            return new McpSchema.ElicitResult(
                    McpSchema.ElicitResult.Action.CANCEL,
                    Map.of()
            );
        }
    }

    /**
     * Console Strategy:
     * Prompts the terminal user with a timeout.
     */
    private McpSchema.ElicitResult promptUserViaConsole(String message) {
        System.out.println("\n=======================================================");
        System.out.println("🚨 MCP ELICITATION CONFIRMATION REQUIRED 🚨");
        System.out.println("Message: " + message);
        System.out.print("Do you confirm this action? [y/N] (" + timeoutSeconds + "s timeout): ");
        System.out.flush();

        try {
            CompletableFuture<String> readLineFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                    return reader.readLine();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            String userInput = readLineFuture.get(timeoutSeconds, TimeUnit.SECONDS);

            if (userInput != null) {
                userInput = userInput.trim().toLowerCase();
            }

            log.info("Console user input received: '{}'", userInput);

            if ("y".equals(userInput) || "yes".equals(userInput)) {
                System.out.println(">> Confirmed by user.");
                return new McpSchema.ElicitResult(
                        McpSchema.ElicitResult.Action.ACCEPT,
                        Map.of("confirm", true)
                );
            } else if ("n".equals(userInput) || "no".equals(userInput)) {
                System.out.println(">> Declined by user.");
                return new McpSchema.ElicitResult(
                        McpSchema.ElicitResult.Action.DECLINE,
                        Map.of("confirm", false)
                );
            } else {
                System.out.println(">> Unrecognized input ('" + userInput + "'). Action cancelled.");
                return new McpSchema.ElicitResult(
                        McpSchema.ElicitResult.Action.CANCEL,
                        Map.of()
                );
            }
        } catch (TimeoutException e) {
            System.out.println("\n>> Elicitation timed out after " + timeoutSeconds + " seconds. Action cancelled.");
            log.warn("Console elicitation timed out waiting for user input.");
            return new McpSchema.ElicitResult(
                    McpSchema.ElicitResult.Action.CANCEL,
                    Map.of()
            );
        } catch (Exception e) {
            System.out.println("\n>> Failed to read input from console: " + e.getMessage());
            log.error("Failed to read user input during console elicitation", e);
            return new McpSchema.ElicitResult(
                    McpSchema.ElicitResult.Action.CANCEL,
                    Map.of()
            );
        }
    }
}
