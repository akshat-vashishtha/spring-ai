package com.learning.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DynamicPromptRequest {

    private String topic;

    @Builder.Default
    private String persona = "Software Architecture & AI Expert";

    @Builder.Default
    private String tone = "professional, concise, and structured";

    @Builder.Default
    private String language = "English";

    @Builder.Default
    private String format = "markdown format with bullet points and code examples where applicable";

    @Builder.Default
    private String additionalInstructions = "Provide clear reasoning and highlight best practices.";
}
