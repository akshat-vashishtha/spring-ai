package com.learning.etl.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.learning.etl.dto.DocumentIngestResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentEtlService {

    private static final Set<String> SUPPORTED_EXTENSIONS = Set.of(
            ".pdf", ".docx", ".doc", ".txt", ".html", ".htm", ".md", ".json"
    );

    private final VectorStore vectorStore;
    private final TokenTextSplitter tokenTextSplitter;

    /**
     * Upload and ingest a single PDF or DOCX file.
     */
    public DocumentIngestResponse uploadAndIngest(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file cannot be empty");
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || (!fileName.toLowerCase().endsWith(".pdf")
                && !fileName.toLowerCase().endsWith(".docx")
                && !fileName.toLowerCase().endsWith(".doc"))) {
            throw new IllegalArgumentException("Unsupported file type for '" + fileName + "'. Only PDF and DOCX files are allowed.");
        }

        return processResource(file.getResource(), fileName);
    }

    /**
     * Batch ingest all supported documents from a server directory path.
     */
    public List<DocumentIngestResponse> ingestFolder(String folderPath) throws IOException {
        if (folderPath == null || folderPath.isBlank()) {
            throw new IllegalArgumentException("Folder path cannot be blank");
        }

        Path folder = Path.of(folderPath);
        if (!Files.isDirectory(folder)) {
            throw new IllegalArgumentException("Folder path does not exist or is not a directory: " + folderPath);
        }

        List<DocumentIngestResponse> results = new ArrayList<>();
        try (var stream = Files.walk(folder)) {
            List<Path> files = stream.filter(Files::isRegularFile)
                    .filter(this::isSupported)
                    .toList();

            for (Path path : files) {
                try {
                    results.add(processResource(new FileSystemResource(path), path.getFileName().toString()));
                } catch (Exception e) {
                    log.warn("Failed to ingest file '{}': {}", path, e.getMessage());
                }
            }
        }

        return results;
    }

    /**
     * Reads document with Tika, chunks text with TokenTextSplitter, and saves to VectorStore.
     */
    private DocumentIngestResponse processResource(Resource resource, String fileName) {
        List<Document> rawDocuments = new TikaDocumentReader(resource).get();
        if (rawDocuments == null || rawDocuments.isEmpty()) {
            throw new IllegalArgumentException("Could not extract any content from '" + fileName + "'");
        }

        List<Document> chunks = tokenTextSplitter.apply(rawDocuments);
        vectorStore.accept(chunks);

        log.info("Ingested '{}': {} chunks saved into vector store", fileName, chunks.size());

        return DocumentIngestResponse.builder()
                .fileName(fileName)
                .totalChunks(chunks.size())
                .message("Successfully parsed, chunked, and stored in vector database.")
                .build();
    }

    private boolean isSupported(Path path) {
        String name = path.getFileName().toString().toLowerCase();
        return SUPPORTED_EXTENSIONS.stream().anyMatch(name::endsWith);
    }
}
