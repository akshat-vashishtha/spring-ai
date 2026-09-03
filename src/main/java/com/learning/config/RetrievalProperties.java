package com.learning.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix = "app.ai.retrieval")
public class RetrievalProperties {

    private int topK;
    private double similarityThreshold;
}
