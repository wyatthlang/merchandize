package kyx.hackathon.merchandize.service;

import com.google.genai.Client;
import com.google.genai.types.*;
import kyx.hackathon.merchandize.model.ImageGenRequest;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
public class ImageGenService {

    public List<String> generate(ImageGenRequest request, String outputDir) {
        Client client = Client.builder()
                .apiKey(System.getenv("GOOGLE_API_KEY"))
                .build();

        String prompt = String.format("""
                    You are an artist creating stickers to be placed on merchandise based on the transcript and image frames from a video clip.
                    Use this video transcription as a reference while generating the stickers: "%s"
                    Use this prompt as additional input from your user: "%s"
                    #IMPORTANT You must generate an image, only one sticker per file, you may provide multiple files, no glare visual artifacts on the images.
                """, request.getTranscription(), request.getPrompt());

        var path = Paths.get(request.getImgPath());

        byte[] bytes = null;
        try {
            bytes = Files.readAllBytes(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Content content = Content.fromParts(
                Part.fromText(prompt),
                Part.fromBytes(bytes, "image/jpeg")
        );
        var response = client.models.generateContent("gemini-2.5-flash-image", content, null);

        int counter = 0;
        List<String> fileNames = new ArrayList();
        for (Candidate candidate : response.candidates().get()) {
            for (Part part : candidate.content().get().parts().get()) {
                if (part.inlineData().isPresent() && part.inlineData().get().mimeType().get().startsWith("image/")) {
                    byte[] imageData = part.inlineData().get().data().get();

                    String fileName = request.getId().toString() + "-" + counter++ + ".png";
                    String fullPath = outputDir + fileName;
                    try (FileOutputStream fos = new FileOutputStream(fullPath)) {
                        fos.write(imageData);
                        fileNames.add(fileName);
                    } catch (FileNotFoundException e) {
                        throw new RuntimeException(e);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }

        return fileNames;
    }

}
