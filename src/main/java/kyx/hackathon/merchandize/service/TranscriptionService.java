package kyx.hackathon.merchandize.service;

import kyx.hackathon.merchandize.model.TranscriptionRequest;
import kyx.hackathon.merchandize.model.TranscriptionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.openai.OpenAiAudioTranscriptionModel;
import org.springframework.ai.openai.OpenAiAudioTranscriptionOptions;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class TranscriptionService {

    private final OpenAiAudioTranscriptionModel openAiAudioTranscriptionModel;

    public TranscriptionResponse transcribe(TranscriptionRequest request) {
        AudioTranscriptionPrompt prompt = new AudioTranscriptionPrompt(
                request.audioFile(),
                OpenAiAudioTranscriptionOptions.builder()
                        .prompt(request.context())
                        .build()
        );
        var response = openAiAudioTranscriptionModel.call(prompt);

        return new TranscriptionResponse(response.getResult().getOutput());
    }
}
