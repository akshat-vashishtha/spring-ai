package com.learning.audiotranscription.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.learning.audiotranscription.service.AudioTranscriptionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/audio/transcription")
@RequiredArgsConstructor
@Tag(name = "Audio Transcription API", description = "Audio speech-to-text transcription powered by OpenAI Whisper")
public class AudioTranscriptionController {

    private final AudioTranscriptionService transcriptionService;

    @GetMapping("/sample")
    @Operation(summary = "Transcribe Sample Audio", description = "Transcribes a pre-configured sample audio file from classpath resources using OpenAI Whisper.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transcribed text result", content = @Content(mediaType = "text/plain", schema = @Schema(type = "string", example = "Hello, welcome to Spring AI audio transcription demo."))),
            @ApiResponse(responseCode = "500", description = "Transcription processing error")
    })
    public String transcribeSample() {
        return transcriptionService.transcribeSampleAudio();
    }

    @PostMapping(value = "/upload/custom", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Transcribe Custom Audio File", description = "Uploads an audio file (mp3, wav, m4a, flac, etc.) and transcribes it to text using OpenAI Whisper with optional guide prompt and language hints.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transcribed text result", content = @Content(mediaType = "text/plain", schema = @Schema(type = "string", example = "This is the transcribed text from the uploaded audio file."))),
            @ApiResponse(responseCode = "400", description = "Invalid audio file"),
            @ApiResponse(responseCode = "500", description = "Transcription service failure")
    })
    public String transcribeUploadCustom(
            @Parameter(description = "Audio file to transcribe (mp3, wav, etc.)", required = true)
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "Optional context prompt to guide spelling or style of transcription", example = "Technical discussion on Spring AI")
            @RequestParam(value = "prompt", required = false) String prompt,
            @Parameter(description = "Optional ISO-639-1 language code of the audio (e.g. en, fr, es, de)", example = "en")
            @RequestParam(value = "language", required = false) String language) {
        return transcriptionService.transcribeAudio(file, prompt, language);
    }
}
