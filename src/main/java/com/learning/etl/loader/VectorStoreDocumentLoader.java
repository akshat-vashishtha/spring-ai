package com.learning.etl.loader;

import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

@Component
public class VectorStoreDocumentLoader implements DocumentLoader {

    private final VectorStore vectorStore;

    public VectorStoreDocumentLoader(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @Override
    public void load(List<Document> documents) {
        if (documents == null || documents.isEmpty()) {
            return;
        }

        vectorStore.add(documents);
    }
}
