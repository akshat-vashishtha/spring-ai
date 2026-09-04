package com.learning.imagegeneration.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request model for generating images")
public class ImageRequest {
    @Schema(description = "The prompt describing the image to generate", example = "A cute puppy playing in the park", requiredMode = Schema.RequiredMode.REQUIRED)
    private String prompt;
}
