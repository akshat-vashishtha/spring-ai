package com.learning.imagegeneration.controller;

import com.learning.dto.ApiResponse;
import com.learning.imagegeneration.dto.ImageRequest;
import com.learning.imagegeneration.dto.ImageResponseDto;
import com.learning.imagegeneration.service.ImageGenerationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/image")
@RequiredArgsConstructor
@Tag(name = "Image Generation API", description = "Endpoints for generating images using Spring AI and OpenAI's DALL-E model")
public class ImageGenerationController {

    private final ImageGenerationService imageGenerationService;

    @PostMapping("/generate")
    @Operation(summary = "Generate image using json body", description = "Generates an image from a text prompt.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully generated image"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error during image generation")
    })
    public ResponseEntity<ApiResponse<ImageResponseDto>> generateImage(@RequestBody ImageRequest request) {
        try {
            ImageResponseDto result = imageGenerationService.generateImage(request);
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/generate")
    @Operation(summary = "Simple image generation via GET", description = "Generates an image from a text prompt query parameter.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully generated image"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error during image generation")
    })
    public ResponseEntity<ApiResponse<ImageResponseDto>> generateImageSimple(
            @Parameter(description = "Prompt describing the image to generate", example = "A cute puppy")
            @RequestParam String prompt) {
        try {
            ImageRequest request = new ImageRequest();
            request.setPrompt(prompt);
            ImageResponseDto result = imageGenerationService.generateImage(request);
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error(e.getMessage()));
        }
    }
}
