package com.learning.etl.config;

import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.learning.etl.model.DocumentType;
import com.learning.etl.reader.DocumentReader;
import com.learning.etl.reader.HtmlDocumentReader;
import com.learning.etl.reader.JsonDocumentReader;
import com.learning.etl.reader.PdfDocumentReader;
import com.learning.etl.transformer.DefaultDocumentTransformer;
import com.learning.etl.transformer.DocumentTransformer;

@Configuration
public class EtlConfig {

    @Bean
    public DocumentTransformer documentTransformer() {
        return new DefaultDocumentTransformer();
    }

    @Bean
    public Map<DocumentType, DocumentReader> documentReaders(
            JsonDocumentReader jsonDocumentReader,
            PdfDocumentReader pdfDocumentReader,
            HtmlDocumentReader htmlDocumentReader) {
        return Map.of(
                DocumentType.JSON, jsonDocumentReader,
                DocumentType.PDF, pdfDocumentReader,
                DocumentType.HTML, htmlDocumentReader);
    }
}
