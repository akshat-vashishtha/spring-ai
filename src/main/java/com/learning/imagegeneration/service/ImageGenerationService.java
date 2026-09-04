package com.learning.imagegeneration.service;

import com.learning.imagegeneration.dto.ImageRequest;
import com.learning.imagegeneration.dto.ImageResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.openai.OpenAiImageOptions;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageGenerationService {

    private final ImageModel imageModel;

    public ImageResponseDto generateImage(ImageRequest request) {
        log.info("Generating image with prompt: '{}', model: {}, quality: {} ({}x{})",
                request.getPrompt(), request.getModel(), request.getQuality(), request.getWidth(), request.getHeight());

        OpenAiImageOptions.Builder optionsBuilder = OpenAiImageOptions.builder();
        if (request.getModel() != null) {
            optionsBuilder.model(request.getModel());
        }
        if (request.getQuality() != null) {
            optionsBuilder.quality(request.getQuality());
        }
        if (request.getWidth() != null) {
            optionsBuilder.width(request.getWidth());
        }
        if (request.getHeight() != null) {
            optionsBuilder.height(request.getHeight());
        }

        ImageResponse response = imageModel.call(
                new ImagePrompt(request.getPrompt(), optionsBuilder.build())
        );

        if (response == null || response.getResult() == null || response.getResult().getOutput() == null) {
            log.warn("Empty response returned from ImageModel for prompt: '{}'", request.getPrompt());
            throw new RuntimeException("Failed to generate image: Empty response from AI model");
        }

        String url = response.getResult().getOutput().getUrl();
        String b64Json = response.getResult().getOutput().getB64Json();
        return new ImageResponseDto(url, b64Json);
    }
}
