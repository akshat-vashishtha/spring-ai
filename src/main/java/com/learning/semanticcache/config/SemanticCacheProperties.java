package com.learning.semanticcache.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.ai.semantic-cache")
public class SemanticCacheProperties {

    private double similarityThreshold;
    private String prefix;
}
