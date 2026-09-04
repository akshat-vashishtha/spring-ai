package com.learning.imagegeneration.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response model for generated images")
public class ImageResponseDto {
    @Schema(description = "The URL of the generated image")
    private String url;

    @Schema(description = "The base64-encoded image data")
    private String b64Json;
}
