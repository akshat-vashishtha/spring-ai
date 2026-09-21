package com.learning.imagegeneration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.Base64;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageGenerationService {

    private final ImageModel imageModel;

    public byte[] generateImage(String prompt) {
        log.info("Generating image for prompt: '{}'", prompt);

        ImageResponse response = imageModel.call(new ImagePrompt(prompt));
        if (response == null || response.getResult() == null || response.getResult().getOutput() == null) {
            throw new RuntimeException("Failed to generate image: Empty response from AI model");
        }

        var output = response.getResult().getOutput();

        if (output.getB64Json() != null && !output.getB64Json().isBlank()) {
            return Base64.getDecoder().decode(output.getB64Json());
        }

        if (output.getUrl() != null && !output.getUrl().isBlank()) {
            try (var in = URI.create(output.getUrl()).toURL().openStream()) {
                return in.readAllBytes();
            } catch (Exception e) {
                throw new RuntimeException("Failed to download image from URL: " + output.getUrl(), e);
            }
        }

        throw new RuntimeException("No image data returned from AI model");
    }
}
