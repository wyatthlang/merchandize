package kyx.hackathon.merchandize.controller;

import kyx.hackathon.merchandize.model.ImageGenRequest;
import kyx.hackathon.merchandize.model.LocalUpload;
import kyx.hackathon.merchandize.model.TranscriptionRequest;
import kyx.hackathon.merchandize.model.VideoClip;
import kyx.hackathon.merchandize.service.ImageGenService;
import kyx.hackathon.merchandize.service.TranscriptionService;
import kyx.hackathon.merchandize.service.VideoDownloadService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class MerchController {

    @Value("${image-directory}")
    private String outputDir;

    private final VideoDownloadService videoDownloadService;
    private final TranscriptionService transcriptionService;
    private final ImageGenService imageGenService;

    @PostMapping("/merch")
    @CrossOrigin(origins = "*")
    public List<String> merch(@RequestBody VideoClip videoClip) throws Exception {
        UUID id = UUID.randomUUID();

        if ("youtube".equals(videoClip.getVideoSource())) {
            videoDownloadService.downloadYoutubeVideo(videoClip, outputDir, id);
        }

        var inputFilePath = outputDir + id.toString() + ".mp4";
        var outputFilePath = outputDir + id.toString() + "-clip" + ".mp4";

        videoDownloadService.clipVideo(videoClip.getStartTime(), videoClip.getDuration(), inputFilePath, outputFilePath);

        videoDownloadService.generateFrameFromClip(outputFilePath, outputDir, id);

        Resource resource = new FileSystemResource(outputFilePath);
        TranscriptionRequest request = new TranscriptionRequest(resource, "transcribe what is said in the video");
        var response = transcriptionService.transcribe(request);
        var transcription = response.transcription();

        System.out.println(transcription);

        ImageGenRequest igr = new ImageGenRequest();
        igr.setPrompt(videoClip.getPrompt());
        igr.setTranscription(transcription);
        igr.setImgPath(outputDir + id.toString() + "-frame1.jpg");
        igr.setId(id);
        var imgResponse = imageGenService.generate(igr, outputDir);
        System.out.println(imgResponse);

        return imgResponse;
    }

    @PostMapping(value = "/local-upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public List<String> merch(@RequestParam MultipartFile file,
                              @RequestParam LocalUpload localUpload) throws IOException, InterruptedException {
        UUID id = UUID.randomUUID();

        var inputFilePath = outputDir + id.toString() + ".mp4";
        var outputFilePath = outputDir + id.toString() + "-clip" + ".mp4";

        Path path = Paths.get(outputDir, file.getOriginalFilename());

        Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
        videoDownloadService.clipVideo(localUpload.getStartTime(), localUpload.getDuration(), inputFilePath, outputFilePath);

        return List.of();
    }
}
