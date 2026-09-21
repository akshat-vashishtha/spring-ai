package com.learning.semanticcache.config;

import org.springframework.ai.chat.cache.semantic.SemanticCache;
import org.springframework.ai.chat.cache.semantic.SemanticCacheAdvisor;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.redis.cache.semantic.DefaultSemanticCache;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import redis.clients.jedis.RedisClient;

@Configuration
public class SemanticCacheConfig {

    @Bean
    public RedisClient jedisClient(@Value("${spring.data.redis.host}") String host,
            @Value("${spring.data.redis.port}") int port) {
        return RedisClient.create(host, port);
    }

    @Bean
    public SemanticCache semanticCache(EmbeddingModel embeddingModel,
            RedisClient jedisClient,
            SemanticCacheProperties properties) {
        return DefaultSemanticCache.builder()
                .embeddingModel(embeddingModel)
                .jedisClient(jedisClient)
                .similarityThreshold(properties.getSimilarityThreshold())
                .prefix(properties.getPrefix())
                .build();
    }

    @Bean
    public SemanticCacheAdvisor semanticCacheAdvisor(SemanticCache semanticCache) {
        return SemanticCacheAdvisor.builder()
                .cache(semanticCache)
                .build();
    }
}
