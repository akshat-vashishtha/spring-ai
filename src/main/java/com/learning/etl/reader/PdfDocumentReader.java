package com.learning.etl.reader;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;

import com.learning.etl.model.DocumentType;

@Component
public class PdfDocumentReader implements DocumentReader {

    @Override
    public DocumentType supportedType() {
        return DocumentType.PDF;
    }

    @Override
    public List<Document> read(Path filePath) throws IOException {
        try (PDDocument document = Loader.loadPDF(filePath.toFile())) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);

            return List.of(Document.builder()
                    .text(normalizeText(text))
                    .metadata(Map.of(
                            "source", filePath.toString(),
                            "documentType", "PDF",
                            "fileName", filePath.getFileName().toString()))
                    .build());
        }
    }

    private String normalizeText(String text) {
        if (text == null) {
            return "";
        }
        return text.replaceAll("\\s+", " ").trim();
    }
}
