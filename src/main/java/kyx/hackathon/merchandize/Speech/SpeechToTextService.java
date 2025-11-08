package kyx.hackathon.merchandize.speech;

import com.theokanning.openai.OpenAiService;
import com.theokanning.openai.audio.CreateTranscriptRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.service;
import org.Springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;

public class SpeechToTextSercie {
    
    private final OpenAiService OpenAiService;

    //speaks with openAI
    public SpeechToTextService(@value("${openai.api.key}") string apiKey){
        this.openAiService= new OpenAiService(apiKey); 
    }

    //sends WAV/MP3 audio to whisper and shoots back the text
    public string transcribeAudio(File audioFile) {
        CreateTranscriptRequest request = CreateTranscriptRequest.builder()
            .model("whisper-1")
            .build();

        return openAiService.createTranscription(request, audioFile).getText();
    }

    //converts from spring Multipart to an mp4 file
    public File convertMultipartToFile(MultipartFile file) throws Exception {
        File convFile = new File(file.getOriginalFilename());
        FileOutputStream fos = new FileOutputStream(convFile);
        fos.write(file.getBytes());
        fos.close();
        return convFile;
    }
}

