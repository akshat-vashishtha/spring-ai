package com.learning.mcp.internal.server.elicitation;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.learning.mcp.internal.model.Product;
import com.learning.mcp.internal.service.ProductService;

import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Server-side MCP Elicitation Handler for Product Deletion Confirmation.
 * Ensures that destructive delete operations ask the connected MCP client for confirmation
 * before making changes to MongoDB.
 */
@Slf4j
@Component
public class DeleteConfirmationElicitor {

    private final ProductService productService;

    public DeleteConfirmationElicitor(ProductService productService) {
        this.productService = productService;
    }

    /**
     * Executes deleteProduct workflow with interactive MCP elicitation confirmation.
     *
     * @param id Product ID to delete
     * @param exchange The active MCP sync server exchange (null when called outside MCP)
     * @return Result message describing the outcome
     */
    public String confirmAndDelete(String id, McpSyncServerExchange exchange) {
        log.info("DeleteConfirmationElicitor: Initiating delete confirmation check for productId: {}", id);

        Optional<Product> productOpt = productService.getProductById(id);
        if (productOpt.isEmpty()) {
            log.warn("Product not found with id: {}. Skipping elicitation.", id);
            return "Cannot delete. Product not found with id: " + id;
        }

        Product product = productOpt.get();

        if (exchange == null) {
            log.warn("Fail-safe triggered: No active McpSyncServerExchange found for productId: {}. Refusing deletion without confirmation.", id);
            return "Product was NOT deleted: Operation requires user confirmation via MCP Elicitation, but no active MCP exchange was present.";
        }

        if (!isElicitationSupported(exchange)) {
            log.warn("Fail-safe triggered: Connected MCP client does not support or advertise elicitation capability for productId: {}", id);
            return "Product was NOT deleted: The connected MCP client does not support MCP Elicitation confirmation.";
        }

        String confirmationMessage = String.format(
                "Are you sure you want to delete product '%s' (id: %s)? This cannot be undone.",
                product.getName(),
                id
        );

        Map<String, Object> requestedSchema = Map.of(
                "type", "object",
                "properties", Map.of(
                        "confirm", Map.of(
                                "type", "boolean",
                                "description", "Set to true to confirm deletion, false to cancel"
                        )
                ),
                "required", List.of("confirm")
        );

        McpSchema.ElicitFormRequest elicitRequest = McpSchema.ElicitFormRequest.builder(confirmationMessage, requestedSchema)
                .build();

        try {
            log.info("Sending MCP elicitation request to client for product '{}' (id: {})", product.getName(), id);
            McpSchema.ElicitResult elicitResult = exchange.createElicitation(elicitRequest);

            if (elicitResult == null) {
                log.warn("Received null elicitation result from client for productId: {}", id);
                return "Product was NOT deleted: Received empty confirmation response from client.";
            }

            log.info("Received elicitation result from client: action={}, content={}",
                    elicitResult.action(), elicitResult.content());

            if (elicitResult.action() == McpSchema.ElicitResult.Action.ACCEPT) {
                Map<String, Object> content = elicitResult.content();
                Object confirmVal = content != null ? content.get("confirm") : null;
                boolean isConfirmed = Boolean.TRUE.equals(confirmVal)
                        || "true".equalsIgnoreCase(String.valueOf(confirmVal));

                if (isConfirmed) {
                    boolean deleted = productService.deleteProduct(id);
                    if (deleted) {
                        log.info("Product successfully deleted following user confirmation: id={}", id);
                        return "Product successfully deleted with id: " + id;
                    }
                    return "Cannot delete. Product not found with id: " + id;
                } else {
                    log.info("User explicitly declined deletion (confirm=false) for productId: {}", id);
                    return String.format("Deletion cancelled for product '%s' (id: %s).", product.getName(), id);
                }
            } else if (elicitResult.action() == McpSchema.ElicitResult.Action.DECLINE) {
                log.info("Client declined elicitation for productId: {}", id);
                return String.format("Deletion cancelled for product '%s' (id: %s).", product.getName(), id);
            } else {
                log.warn("Elicitation was cancelled or timed out for productId: {}", id);
                return String.format("Deletion cancelled for product '%s' (id: %s).", product.getName(), id);
            }
        } catch (Exception e) {
            log.error("Fail-safe triggered: Exception during MCP elicitation for productId: {}", id, e);
            return "Product was NOT deleted: MCP Elicitation error: " + e.getMessage();
        }
    }

    /**
     * Checks if the connected client declares elicitation capability.
     */
    private boolean isElicitationSupported(McpSyncServerExchange exchange) {
        return exchange != null
                && exchange.getClientCapabilities() != null
                && exchange.getClientCapabilities().elicitation() != null;
    }
}
