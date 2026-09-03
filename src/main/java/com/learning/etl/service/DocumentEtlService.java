package com.learning.etl.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import com.learning.etl.loader.DocumentLoader;
import com.learning.etl.model.DocumentType;
import com.learning.etl.reader.DocumentReader;
import com.learning.etl.transformer.DocumentTransformer;

@Service
public class DocumentEtlService {

    private final Map<DocumentType, DocumentReader> readers;
    private final DocumentTransformer transformer;
    private final DocumentLoader loader;

    public DocumentEtlService(Map<DocumentType, DocumentReader> readers,
                             DocumentTransformer transformer,
                             DocumentLoader loader) {
        this.readers = readers;
        this.transformer = transformer;
        this.loader = loader;
    }

    public List<Document> ingestFolder(String folderPath) throws IOException {
        if (folderPath == null || folderPath.isBlank()) {
            throw new IllegalArgumentException("Folder path cannot be blank");
        }

        Path folder = Path.of(folderPath);
        List<Document> allChunks = new ArrayList<>();

        try (var files = Files.walk(folder)) {
            for (Path file : files.filter(Files::isRegularFile).toList()) {
                try {
                    List<Document> fileChunks = ingestFile(file);
                    allChunks.addAll(fileChunks);
                } catch (IllegalArgumentException ignored) {
                    // Ignore unsupported file types in the folder
                }
            }
        }

        return allChunks;
    }

    public List<Document> ingestFile(Path filePath) throws IOException {
        DocumentType type = DocumentType.fromPath(filePath);
        DocumentReader reader = readers.get(type);

        if (reader == null) {
            throw new IllegalArgumentException("No reader found for type: " + type);
        }

        List<Document> rawDocuments = reader.read(filePath);
        List<Document> transformedDocuments = transformer.transform(rawDocuments);
        loader.load(transformedDocuments);
        return transformedDocuments;
    }
}
