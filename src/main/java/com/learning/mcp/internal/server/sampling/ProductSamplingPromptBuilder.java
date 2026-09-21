package com.learning.mcp.internal.server.sampling;

import com.learning.mcp.internal.model.Product;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

/**
 * Helper class responsible for building LLM prompts used in MCP Sampling operations.
 */
@Component
public class ProductSamplingPromptBuilder {

    public static final String DESCRIPTION_SYSTEM_PROMPT =
            "You are an expert e-commerce copywriter. Generate concise, engaging, and professional product descriptions.";

    public static final String CATALOG_SYSTEM_PROMPT =
            "You are an inventory and catalog analyst. Provide structured, executive-ready summaries of product catalogs.";

    /**
     * Builds the prompt for generating a product marketing description.
     */
    public String buildProductDescriptionPrompt(Product product) {
        return String.format(
                "Write a compelling 2-3 sentence marketing description for the following product:\n"
                        + "- Name: %s\n"
                        + "- Category: %s\n"
                        + "- Price: $%.2f\n"
                        + "- Current Stock: %d\n"
                        + (product.getDescription() != null && !product.getDescription().isBlank()
                        ? "- Existing Notes: " + product.getDescription() + "\n" : "")
                        + "\nHighlight its value proposition and target audience.",
                product.getName(),
                product.getCategory(),
                product.getPrice() != null ? product.getPrice() : 0.0,
                product.getStock() != null ? product.getStock() : 0
        );
    }

    /**
     * Builds the prompt for summarizing the entire product catalog.
     */
    public String buildCatalogSummaryPrompt(List<Product> products) {
        String productListFormatted = products.stream()
                .map(p -> String.format("- %s (Category: %s, Price: $%.2f, Stock: %d)",
                        p.getName(),
                        p.getCategory() != null ? p.getCategory() : "General",
                        p.getPrice() != null ? p.getPrice() : 0.0,
                        p.getStock() != null ? p.getStock() : 0))
                .collect(Collectors.joining("\n"));

        return "Analyze and summarize the following product catalog:\n\n"
                + productListFormatted
                + "\n\nPlease provide:\n"
                + "1. Total number of products and active categories\n"
                + "2. Price overview (highest, lowest, average)\n"
                + "3. Low-stock warnings (items with stock < 10)\n"
                + "4. Key business observations in 2-3 bullet points.";
    }
}
