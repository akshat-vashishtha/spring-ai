package com.learning.etl.transformer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.ai.document.Document;

public class DefaultDocumentTransformer implements DocumentTransformer {

    private static final int CHUNK_SIZE = 800;

    @Override
    public List<Document> transform(List<Document> documents) {
        List<Document> transformedDocuments = new ArrayList<>();

        for (Document document : documents) {
            String cleanText = cleanText(document.getText());
            if (cleanText.isBlank()) {
                continue;
            }

            List<String> chunks = splitIntoChunks(cleanText, CHUNK_SIZE);

            for (int i = 0; i < chunks.size(); i++) {
                Map<String, Object> metadata = new HashMap<>();
                if (document.getMetadata() != null) {
                    metadata.putAll(document.getMetadata());
                }
                metadata.put("chunkIndex", i);
                metadata.put("chunkCount", chunks.size());

                transformedDocuments.add(Document.builder()
                        .text(chunks.get(i))
                        .metadata(metadata)
                        .build());
            }
        }

        return transformedDocuments;
    }

    private String cleanText(String text) {
        if (text == null) {
            return "";
        }

        return text.replace("\r\n", "\n")
                .replace("\n", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private List<String> splitIntoChunks(String text, int chunkSize) {
        List<String> chunks = new ArrayList<>();
        int start = 0;

        while (start < text.length()) {
            int end = Math.min(start + chunkSize, text.length());
            chunks.add(text.substring(start, end).trim());
            start = end;
        }

        return chunks;
    }
}
