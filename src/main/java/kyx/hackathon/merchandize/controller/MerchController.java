package kyx.hackathon.merchandize.controller;

import kyx.hackathon.merchandize.model.ImageGenRequest;
import kyx.hackathon.merchandize.model.TranscriptionRequest;
import kyx.hackathon.merchandize.model.VideoClip;
import kyx.hackathon.merchandize.service.ImageGenService;
import kyx.hackathon.merchandize.service.VideoDownloadService;
import kyx.hackathon.merchandize.service.TranscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

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
    public void merch(@RequestBody VideoClip videoClip) throws Exception {
        UUID id = UUID.randomUUID();

        if ("youtube".equals(videoClip.getVideoSource())) {
            videoDownloadService.downloadYoutubeVideo(videoClip, outputDir, id);
        }

        var inputFilePath = outputDir + id.toString() + ".mp4";
        var outputFilePath = outputDir + id.toString() + "-clip" + ".mp4";

        videoDownloadService.clipVideo(videoClip, inputFilePath, outputFilePath);

        ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg",
                "-i", inputFilePath,
                "-ss", "00:00:01",
                "-frames:v", "1",
                outputDir + id.toString() + "-frame1.jpg"
        );

        pb.redirectError();

        var p = pb.start();
        p.waitFor();

        Resource resource = new FileSystemResource(outputFilePath);
        TranscriptionRequest request = new TranscriptionRequest(resource, "transcribe what is said in the video");
        var response = transcriptionService.transcribe(request);
        var transcription = response.transcription();

        System.out.println(transcription);

        ImageGenRequest igr = new ImageGenRequest();
        igr.setTranscription(transcription);
        igr.setImgPath(outputDir + id.toString() + "-frame1.jpg");
        igr.setId(id);
        var imgResponse = imageGenService.generate(igr, outputDir);
        System.out.println(imgResponse);


    }
}
