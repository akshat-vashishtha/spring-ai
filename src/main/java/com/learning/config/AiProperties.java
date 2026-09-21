package com.learning.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix = "app.ai")
public class AiProperties {

    private Safeguard safeguard = new Safeguard();

    @Data
    public static class Safeguard {
        private List<String> sensitiveWords = new ArrayList<>();
        private String failureResponse;
    }
}
