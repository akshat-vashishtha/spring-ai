package com.learning.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request model for dynamic prompt templating")
public class DynamicPromptRequest {

    @Schema(description = "The main subject or question for the prompt", example = "Microservices vs Monolith architecture", requiredMode = Schema.RequiredMode.REQUIRED)
    private String topic;

    @Schema(description = "Persona/role that the AI should adopt", example = "Software Architecture & AI Expert", defaultValue = "Software Architecture & AI Expert")
    @Builder.Default
    private String persona = "Software Architecture & AI Expert";

    @Schema(description = "Tone of the response", example = "professional, concise, and structured", defaultValue = "professional, concise, and structured")
    @Builder.Default
    private String tone = "professional, concise, and structured";

    @Schema(description = "Output language", example = "English", defaultValue = "English")
    @Builder.Default
    private String language = "English";

    @Schema(description = "Desired format of the response", example = "markdown format with bullet points and code examples where applicable", defaultValue = "markdown format with bullet points and code examples where applicable")
    @Builder.Default
    private String format = "markdown format with bullet points and code examples where applicable";

    @Schema(description = "Additional custom instructions for the model", example = "Provide clear reasoning and highlight best practices.", defaultValue = "Provide clear reasoning and highlight best practices.")
    @Builder.Default
    private String additionalInstructions = "Provide clear reasoning and highlight best practices.";
}
