package com.learning.etl.transformer;

import java.util.List;

import org.springframework.ai.document.Document;

public interface DocumentTransformer {
    List<Document> transform(List<Document> documents);
}
