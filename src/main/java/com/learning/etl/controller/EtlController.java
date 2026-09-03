package com.learning.etl.controller;

import java.io.IOException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.learning.etl.service.DocumentEtlService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/etl")
@Tag(name = "ETL Pipeline API", description = "Batch document ingestion pipeline supporting PDF, JSON, DOCX, Markdown, Text, and HTML")
public class EtlController {

    private final DocumentEtlService documentEtlService;

    public EtlController(DocumentEtlService documentEtlService) {
        this.documentEtlService = documentEtlService;
    }

    @PostMapping("/ingest-folder")
    @Operation(summary = "Batch Ingest Documents from Folder", description = "Reads all supported files (PDF, JSON, TXT, MD, HTML, DOCX) from a server folder path, chunks them using token-based text splitters, generates embeddings, and saves them to Qdrant vector store.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Folder successfully ingested and indexed; returns count of chunks loaded"),
            @ApiResponse(responseCode = "400", description = "Invalid folder path or read error")
    })
    public ResponseEntity<String> ingestFolder(
            @Parameter(description = "Absolute or relative directory path containing documents to ingest", example = "src/main/resources/documents")
            @RequestParam String folderPath) {
        try {
            int totalChunks = documentEtlService.ingestFolder(folderPath).size();
            return ResponseEntity.ok("ETL done. Folder: " + folderPath + ", chunks loaded: " + totalChunks);
        } catch (IllegalArgumentException | IOException e) {
            return ResponseEntity.badRequest().body("ETL failed: " + e.getMessage());
        }
    }
}
