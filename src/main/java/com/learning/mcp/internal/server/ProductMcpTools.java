package com.learning.mcp.internal.server;

import java.util.List;
import java.util.Optional;

import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.learning.mcp.internal.model.Product;
import com.learning.mcp.internal.service.ProductService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * MCP Server Tools for Internal Product Catalog Management.
 * Exposes MongoDB Product CRUD operations over the Model Context Protocol (MCP).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProductMcpTools {

    private final ProductService productService;
    private final ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    @Tool(description = "Add a new product to the catalog with name, description, price, category, and stock quantity.")
    @McpTool(name = "addProduct", description = "Add a new product to the catalog with name, description, price, category, and stock quantity.")
    public String addProduct(
            @ToolParam(description = "Product name") @McpToolParam(description = "Product name", required = true) String name,
            @ToolParam(description = "Product description") @McpToolParam(description = "Product description", required = false) String description,
            @ToolParam(description = "Product price in USD") @McpToolParam(description = "Product price in USD", required = true) double price,
            @ToolParam(description = "Product category (e.g. Electronics, Books, Fashion)") @McpToolParam(description = "Product category", required = false) String category,
            @ToolParam(description = "Initial stock quantity") @McpToolParam(description = "Initial stock quantity", required = false) Integer stock) {
        log.info("MCP Tool addProduct called: name={}, price={}, category={}", name, price, category);

        Product product = Product.builder()
                .name(name)
                .description(description)
                .price(price)
                .category(category != null ? category : "General")
                .stock(stock != null ? stock : 0)
                .build();

        Product saved = productService.addProduct(product);
        return toJson(saved);
    }

    @Tool(description = "Get details of a specific product from the catalog by its unique product ID.")
    @McpTool(name = "getProduct", description = "Get details of a specific product from the catalog by its unique product ID.")
    public String getProduct(
            @ToolParam(description = "Unique product ID") @McpToolParam(description = "Unique product ID", required = true) String id) {
        log.info("MCP Tool getProduct called: id={}", id);

        Optional<Product> product = productService.getProductById(id);
        if (product.isPresent()) {
            return toJson(product.get());
        }
        return "Product not found with id: " + id;
    }

    @Tool(description = "Retrieve all products currently stored in the product catalog database.")
    @McpTool(name = "getAllProducts", description = "Retrieve all products currently stored in the product catalog database.")
    public String getAllProducts() {
        log.info("MCP Tool getAllProducts called");

        List<Product> products = productService.getAllProducts();
        if (products.isEmpty()) {
            return "No products found in the catalog.";
        }
        return toJson(products);
    }

    @Tool(description = "Update details of an existing product in the catalog (price, stock, name, description, category).")
    @McpTool(name = "updateProduct", description = "Update details of an existing product in the catalog.")
    public String updateProduct(
            @ToolParam(description = "Unique product ID to update") @McpToolParam(description = "Unique product ID to update", required = true) String id,
            @ToolParam(description = "New product name (optional)") @McpToolParam(description = "New product name (optional)", required = false) String name,
            @ToolParam(description = "New description (optional)") @McpToolParam(description = "New description (optional)", required = false) String description,
            @ToolParam(description = "New price (optional)") @McpToolParam(description = "New price (optional)", required = false) Double price,
            @ToolParam(description = "New category (optional)") @McpToolParam(description = "New category (optional)", required = false) String category,
            @ToolParam(description = "New stock quantity (optional)") @McpToolParam(description = "New stock quantity (optional)", required = false) Integer stock) {
        log.info("MCP Tool updateProduct called for id: {}", id);

        Product updateData = Product.builder()
                .name(name)
                .description(description)
                .price(price)
                .category(category)
                .stock(stock)
                .build();

        Optional<Product> updated = productService.updateProduct(id, updateData);
        if (updated.isPresent()) {
            return toJson(updated.get());
        }
        return "Failed to update. Product not found with id: " + id;
    }

    @Tool(description = "Delete a product from the catalog by its unique product ID.")
    @McpTool(name = "deleteProduct", description = "Delete a product from the catalog by its unique product ID.")
    public String deleteProduct(
            @ToolParam(description = "Unique product ID to delete") @McpToolParam(description = "Unique product ID to delete", required = true) String id) {
        log.info("MCP Tool deleteProduct called for id: {}", id);

        boolean deleted = productService.deleteProduct(id);
        if (deleted) {
            return "Product successfully deleted with id: " + id;
        }
        return "Cannot delete. Product not found with id: " + id;
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return String.valueOf(obj);
        }
    }
}
