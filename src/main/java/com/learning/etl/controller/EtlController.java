package com.learning.etl.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.learning.common.dto.ApiResponse;
import com.learning.etl.dto.DocumentIngestResponse;
import com.learning.etl.service.DocumentEtlService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/etl")
@RequiredArgsConstructor
@Tag(name = "ETL Pipeline API", description = "Document ingestion pipeline powered by Spring AI Apache Tika and TokenTextSplitter")
public class EtlController {

    private final DocumentEtlService documentEtlService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload and Ingest PDF or DOCX Document",
               description = "Uploads a PDF or DOCX file, extracts text and metadata using Apache Tika, chunks text into semantic tokens using TokenTextSplitter, and saves embeddings into Qdrant Vector Store.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Document successfully parsed, chunked, and loaded into vector store",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                       schema = @Schema(implementation = DocumentIngestResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid file, unsupported file type, or empty document"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "Document parsing or vector store indexing failure")
    })
    public ResponseEntity<ApiResponse<DocumentIngestResponse>> uploadDocument(
            @Parameter(description = "PDF or DOCX file to upload and ingest", required = true)
            @RequestParam("file") MultipartFile file) {
        DocumentIngestResponse response = documentEtlService.uploadAndIngest(file);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/ingest-folder")
    @Operation(summary = "Batch Ingest Documents from Folder",
               description = "Reads all supported files (PDF, DOCX, TXT, HTML, MD, JSON) from a server folder path, extracts text with Apache Tika, chunks them using TokenTextSplitter, and saves embeddings into Qdrant Vector Store.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Folder successfully scanned and documents indexed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid folder path or read error")
    })
    public ResponseEntity<ApiResponse<List<DocumentIngestResponse>>> ingestFolder(
            @Parameter(description = "Absolute or relative directory path containing documents to ingest", example = "src/main/resources/documents")
            @RequestParam String folderPath) throws IOException {
        List<DocumentIngestResponse> responses = documentEtlService.ingestFolder(folderPath);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }
}
