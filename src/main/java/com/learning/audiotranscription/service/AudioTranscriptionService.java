package com.learning.audiotranscription.service;

import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.audio.transcription.AudioTranscriptionResponse;
import org.springframework.ai.audio.transcription.TranscriptionModel;
import org.springframework.ai.openai.OpenAiAudioTranscriptionOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AudioTranscriptionService {

    private final TranscriptionModel transcriptionModel;

    @Value("classpath:audio/sample.wav")
    private Resource defaultSampleAudioResource;

    public String transcribeSampleAudio() {
        AudioTranscriptionPrompt prompt = new AudioTranscriptionPrompt(defaultSampleAudioResource);
        AudioTranscriptionResponse response = transcriptionModel.call(prompt);
        return response.getResult().getOutput();
    }

    public String transcribeAudio(MultipartFile file) {
        return transcribeAudio(file, null, null);
    }

    public String transcribeAudio(MultipartFile file, String promptText, String language) {
        OpenAiAudioTranscriptionOptions.Builder optionsBuilder = OpenAiAudioTranscriptionOptions.builder();

        if (promptText != null && !promptText.isBlank()) {
            optionsBuilder.prompt(promptText.trim());
        }
        if (language != null && !language.isBlank()) {
            optionsBuilder.language(language.trim());
        }

        AudioTranscriptionPrompt prompt = new AudioTranscriptionPrompt(file.getResource(), optionsBuilder.build());
        AudioTranscriptionResponse response = transcriptionModel.call(prompt);
        return response.getResult().getOutput();
    }
}
