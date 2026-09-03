package com.learning.etl.reader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;

import com.learning.etl.model.DocumentType;

@Component
public class HtmlDocumentReader implements DocumentReader {

    @Override
    public DocumentType supportedType() {
        return DocumentType.HTML;
    }

    @Override
    public List<Document> read(Path filePath) throws IOException {
        org.jsoup.nodes.Document html = Jsoup.parse(filePath.toFile(), StandardCharsets.UTF_8.name());
        String text = html.body() != null ? html.body().text() : html.text();

        return List.of(Document.builder()
                .text(text)
                .metadata(Map.of(
                        "source", filePath.toString(),
                        "documentType", "HTML",
                        "fileName", filePath.getFileName().toString()))
                .build());
    }
}
