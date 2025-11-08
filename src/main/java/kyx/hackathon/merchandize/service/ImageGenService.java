package kyx.hackathon.merchandize.service;

import com.google.genai.Client;
import com.google.genai.types.*;
import kyx.hackathon.merchandize.model.ImageGenRequest;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class ImageGenService {

    public String generate(ImageGenRequest request, String outputDir) {
        Client client = Client.builder()
                .apiKey(System.getenv("GOOGLE_API_KEY"))
                .build();

        String prompt = String.format("""
                    You are an artist creating stickers to be placed on merchandise based on the transcript and image frames from a video clip.
                    Use this video transcription as a reference while generating the stickers: "%s"
                    Use this prompt as additional input from your user: "%s"
                    #IMPORTANT You must generate an image, only one sticker per file, you may provide multiple files, no glare visual artifacts on the images.
                """, request.getTranscription(), request.getPrompt());

        Content content = Content.fromParts(
                Part.fromText(prompt)
//                Part.fromUri("file:/" + request.getImgPath(), "image/jpeg")
        );
        var response = client.models.generateContent("gemini-2.5-flash-image", content, null);

        int counter = 0;
        for (Candidate candidate : response.candidates().get()) {
            for (Part part : candidate.content().get().parts().get()) {
                if (part.inlineData().isPresent() && part.inlineData().get().mimeType().get().startsWith("image/")) {
                    byte[] imageData = part.inlineData().get().data().get();
                    String mimeType = part.inlineData().get().mimeType().get();

                    try (FileOutputStream fos = new FileOutputStream(outputDir + request.getId().toString() + "-" + counter++ + ".png")) {
                        fos.write(imageData);
                    } catch (FileNotFoundException e) {
                        throw new RuntimeException(e);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }

        return response.text();
    }

}
