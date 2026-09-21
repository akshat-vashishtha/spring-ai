package com.learning.mcp.internal.server.sampling;

import java.util.List;
import java.util.Optional;

import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import com.learning.mcp.internal.model.Product;
import com.learning.mcp.internal.service.ProductService;

import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * MCP Server Tools leveraging Client-Side LLM Sampling.
 * Allows the internal MCP server to delegate text generation tasks back to the connected client.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProductSamplingTools {

    private final ProductService productService;
    private final ProductSamplingPromptBuilder promptBuilder;

    /**
     * Generates a marketing product description using MCP sampling on the client's LLM.
     * The generated description is returned to the caller and NOT persisted into MongoDB.
     */
    @McpTool(name = "generateProductDescription", description = "Generates a compelling marketing description for a product by requesting LLM sampling from the connected MCP client.")
    public String generateProductDescription(
            @McpToolParam(description = "Product ID to generate description for", required = true) String productId,
            McpSyncServerExchange exchange) {

        log.info("MCP Tool generateProductDescription called for productId: {}", productId);

        if (!isSamplingSupported(exchange)) {
            log.warn("Sampling rejected: Connected MCP client does not support or declare sampling capability.");
            return "Sampling is not supported by the connected MCP client. Please enable sampling capability on the client side.";
        }

        Optional<Product> productOpt = productService.getProductById(productId);
        if (productOpt.isEmpty()) {
            log.warn("Product with ID '{}' not found", productId);
            return "Product not found with ID: " + productId;
        }

        Product product = productOpt.get();
        String userPrompt = promptBuilder.buildProductDescriptionPrompt(product);

        try {
            log.info("Sending sampling request to client LLM for product '{}' (id: {})", product.getName(), productId);

            List<McpSchema.SamplingMessage> messages = List.of(new McpSchema.SamplingMessage(
                    McpSchema.Role.USER,
                    McpSchema.TextContent.builder(userPrompt).build()
            ));

            McpSchema.CreateMessageRequest request = McpSchema.CreateMessageRequest.builder(messages, 400)
                    .systemPrompt(ProductSamplingPromptBuilder.DESCRIPTION_SYSTEM_PROMPT)
                    .temperature(0.3)
                    .build();

            McpSchema.CreateMessageResult result = exchange.createMessage(request);

            log.info("Received sampling response from client LLM. Model: {}, StopReason: {}",
                    result != null ? result.model() : "unknown",
                    result != null ? result.stopReason() : "unknown");

            return extractTextContent(result);
        } catch (Exception e) {
            log.error("Failed to execute LLM sampling with client for productId: {}", productId, e);
            return "Failed to generate product description via client sampling: " + e.getMessage();
        }
    }

    /**
     * Summarizes the entire product catalog (categories, price range, low-stock items)
     * using MCP sampling on the client's LLM.
     */
    @McpTool(name = "summarizeCatalog", description = "Loads all products and asks the connected MCP client's LLM via sampling to produce a summary of categories, prices, and low-stock items.")
    public String summarizeCatalog(McpSyncServerExchange exchange) {

        log.info("MCP Tool summarizeCatalog called");

        if (!isSamplingSupported(exchange)) {
            log.warn("Sampling rejected: Connected MCP client does not support or declare sampling capability.");
            return "Sampling is not supported by the connected MCP client. Please enable sampling capability on the client side.";
        }

        List<Product> products = productService.getAllProducts();
        if (products.isEmpty()) {
            return "Catalog is empty. No products available to summarize.";
        }

        String userPrompt = promptBuilder.buildCatalogSummaryPrompt(products);

        try {
            log.info("Sending catalog summary sampling request to client LLM for {} products", products.size());

            List<McpSchema.SamplingMessage> messages = List.of(new McpSchema.SamplingMessage(
                    McpSchema.Role.USER,
                    McpSchema.TextContent.builder(userPrompt).build()
            ));

            McpSchema.CreateMessageRequest request = McpSchema.CreateMessageRequest.builder(messages, 800)
                    .systemPrompt(ProductSamplingPromptBuilder.CATALOG_SYSTEM_PROMPT)
                    .temperature(0.2)
                    .build();

            McpSchema.CreateMessageResult result = exchange.createMessage(request);

            log.info("Received catalog summary sampling response from client LLM. Model: {}, StopReason: {}",
                    result != null ? result.model() : "unknown",
                    result != null ? result.stopReason() : "unknown");

            return extractTextContent(result);
        } catch (Exception e) {
            log.error("Failed to execute catalog summary via client sampling", e);
            return "Failed to summarize catalog via client sampling: " + e.getMessage();
        }
    }

    /**
     * Checks whether the connected client declared sampling support in its capabilities.
     */
    private boolean isSamplingSupported(McpSyncServerExchange exchange) {
        return exchange != null
                && exchange.getClientCapabilities() != null
                && exchange.getClientCapabilities().sampling() != null;
    }

    /**
     * Extracts text content safely from the sampling result.
     */
    private String extractTextContent(McpSchema.CreateMessageResult result) {
        if (result == null || result.content() == null) {
            return "No content received from client sampling response.";
        }

        if (result.content() instanceof McpSchema.TextContent textContent) {
            return textContent.text();
        }

        return result.content().toString();
    }
}
