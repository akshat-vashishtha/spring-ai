package com.learning.imagegeneration.controller;

import com.learning.imagegeneration.dto.ImageRequest;
import com.learning.imagegeneration.service.ImageGenerationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/image")
@RequiredArgsConstructor
@Tag(name = "Image Generation API", description = "Endpoints to generate and download images")
public class ImageGenerationController {

    private final ImageGenerationService imageGenerationService;

    @GetMapping(value = "/download", produces = MediaType.IMAGE_PNG_VALUE)
    @Operation(summary = "Generate and download image", description = "Generates an image from prompt and triggers download")
    public ResponseEntity<byte[]> downloadImage(@RequestParam String prompt) {
        byte[] image = imageGenerationService.generateImage(prompt);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"generated-image.png\"")
                .body(image);
    }

    @PostMapping(value = "/download", produces = MediaType.IMAGE_PNG_VALUE)
    @Operation(summary = "Generate and download image via JSON body")
    public ResponseEntity<byte[]> downloadImageBody(@RequestBody ImageRequest request) {
        return downloadImage(request.getPrompt());
    }

    @GetMapping(value = "/generate", produces = MediaType.IMAGE_PNG_VALUE)
    @Operation(summary = "Generate and view image", description = "Generates an image and renders it directly in the browser")
    public ResponseEntity<byte[]> viewImage(@RequestParam String prompt) {
        byte[] image = imageGenerationService.generateImage(prompt);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"generated-image.png\"")
                .body(image);
    }

    @PostMapping(value = "/generate", produces = MediaType.IMAGE_PNG_VALUE)
    @Operation(summary = "Generate and view image via JSON body")
    public ResponseEntity<byte[]> viewImageBody(@RequestBody ImageRequest request) {
        return viewImage(request.getPrompt());
    }
}
