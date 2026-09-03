package com.learning.etl.model;

import java.nio.file.Path;

public enum DocumentType {
    JSON,
    PDF,
    HTML;

    public static DocumentType fromPath(Path path) {
        String fileName = path.getFileName().toString().toLowerCase();

        if (fileName.endsWith(".json")) {
            return JSON;
        }
        if (fileName.endsWith(".pdf")) {
            return PDF;
        }
        if (fileName.endsWith(".html") || fileName.endsWith(".htm")) {
            return HTML;
        }

        throw new IllegalArgumentException("Unsupported document type: " + fileName);
    }
}
