package com.learning.mcp.internal.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.learning.mcp.internal.model.Product;

/**
 * Spring Data MongoDB Repository for internal Product CRUD operations.
 */
@Repository
public interface ProductRepository extends MongoRepository<Product, String> {

    Optional<Product> findByNameIgnoreCase(String name);
}
