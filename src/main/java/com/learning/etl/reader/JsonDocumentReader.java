package com.learning.etl.reader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learning.etl.model.DocumentType;

@Component
public class JsonDocumentReader implements DocumentReader {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public DocumentType supportedType() {
        return DocumentType.JSON;
    }

    @Override
    public List<Document> read(Path filePath) throws IOException {
        JsonNode root = objectMapper.readTree(Files.newInputStream(filePath));
        String text = flattenJson(root);

        return List.of(Document.builder()
                .text(text)
                .metadata(Map.of(
                        "source", filePath.toString(),
                        "documentType", "JSON",
                        "fileName", filePath.getFileName().toString()))
                .build());
    }

    private String flattenJson(JsonNode node) {
        if (node == null || node.isNull()) {
            return "";
        }

        if (node.isValueNode()) {
            return node.asText();
        }

        if (node.isArray()) {
            StringJoiner joiner = new StringJoiner("\n");
            node.forEach(item -> {
                String itemText = flattenJson(item);
                if (!itemText.isBlank()) {
                    joiner.add(itemText);
                }
            });
            return joiner.toString();
        }

        StringJoiner joiner = new StringJoiner("\n");
        node.fields().forEachRemaining(entry -> {
            String key = entry.getKey();
            String value = flattenJson(entry.getValue());
            if (!value.isBlank()) {
                joiner.add(key + ": " + value);
            }
        });
       return joiner.toString();
    }
}
