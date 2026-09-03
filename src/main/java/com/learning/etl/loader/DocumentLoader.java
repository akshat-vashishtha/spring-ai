package com.learning.etl.loader;

import java.util.List;

import org.springframework.ai.document.Document;

public interface DocumentLoader {
    void load(List<Document> documents);
}
