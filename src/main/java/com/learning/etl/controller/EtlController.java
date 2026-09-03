package com.learning.etl.controller;

import java.io.IOException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.learning.etl.service.DocumentEtlService;

@RestController
@RequestMapping("/api/etl")
public class EtlController {

    private final DocumentEtlService documentEtlService;

    public EtlController(DocumentEtlService documentEtlService) {
        this.documentEtlService = documentEtlService;
    }

    @PostMapping("/ingest-folder")
    public ResponseEntity<String> ingestFolder(@RequestParam String folderPath) {
        try {
            int totalChunks = documentEtlService.ingestFolder(folderPath).size();
            return ResponseEntity.ok("ETL done. Folder: " + folderPath + ", chunks loaded: " + totalChunks);
        } catch (IllegalArgumentException | IOException e) {
            return ResponseEntity.badRequest().body("ETL failed: " + e.getMessage());
        }
    }
}
