package com.learning.audiotranscription.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.learning.audiotranscription.service.AudioTranscriptionService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/audio/transcription")
@RequiredArgsConstructor
public class AudioTranscriptionController {

    private final AudioTranscriptionService transcriptionService;

    @GetMapping("/sample")
    public String transcribeSample() {
        return transcriptionService.transcribeSampleAudio();
    }

    @PostMapping(value = "/upload/custom", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String transcribeUploadCustom(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "prompt", required = false) String prompt,
            @RequestParam(value = "language", required = false) String language) {
        return transcriptionService.transcribeAudio(file, prompt, language);
    }
}
