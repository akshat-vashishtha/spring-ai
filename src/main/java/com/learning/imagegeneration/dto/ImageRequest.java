package com.learning.imagegeneration.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Request model for generating images")
public class ImageRequest {
    @Schema(description = "The prompt describing the image to generate", example = "A futuristic city in the style of cyberpunk with neon lights", requiredMode = Schema.RequiredMode.REQUIRED)
    private String prompt;

    @Schema(description = "Model to use for generation (e.g., dall-e-3)", defaultValue = "dall-e-3")
    private String model;

    @Schema(description = "Quality of the generated image (standard or hd)", defaultValue = "standard")
    private String quality;

    @Schema(description = "Width of the generated image", defaultValue = "1024")
    private Integer width;

    @Schema(description = "Height of the generated image", defaultValue = "1024")
    private Integer height;
}
