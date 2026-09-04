package com.learning.mcp.internal.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.learning.mcp.internal.model.Product;
import com.learning.mcp.internal.repository.ProductRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service providing standard CRUD operations on the internal Product MongoDB collection.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    /**
     * Create / Add a new product.
     */
    public Product addProduct(Product product) {
        log.info("Adding new product: {}", product.getName());
        return productRepository.save(product);
    }

    /**
     * Get a product by ID.
     */
    public Optional<Product> getProductById(String id) {
        log.info("Fetching product with id: {}", id);
        return productRepository.findById(id);
    }

    /**
     * Get all products in the catalog.
     */
    public List<Product> getAllProducts() {
        log.info("Fetching all products");
        return productRepository.findAll();
    }

    /**
     * Update an existing product.
     */
    public Optional<Product> updateProduct(String id, Product updatedProduct) {
        log.info("Updating product with id: {}", id);
        return productRepository.findById(id).map(existing -> {
            if (updatedProduct.getName() != null) {
                existing.setName(updatedProduct.getName());
            }
            if (updatedProduct.getDescription() != null) {
                existing.setDescription(updatedProduct.getDescription());
            }
            if (updatedProduct.getPrice() != null) {
                existing.setPrice(updatedProduct.getPrice());
            }
            if (updatedProduct.getCategory() != null) {
                existing.setCategory(updatedProduct.getCategory());
            }
            if (updatedProduct.getStock() != null) {
                existing.setStock(updatedProduct.getStock());
            }
            return productRepository.save(existing);
        });
    }

    /**
     * Delete a product by ID.
     */
    public boolean deleteProduct(String id) {
        log.info("Deleting product with id: {}", id);
        if (productRepository.existsById(id)) {
            productRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
