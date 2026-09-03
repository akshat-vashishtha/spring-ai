package com.learning.etl.reader;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.springframework.ai.document.Document;

import com.learning.etl.model.DocumentType;

public interface DocumentReader {
    DocumentType supportedType();

    List<Document> read(Path filePath) throws IOException;
}
