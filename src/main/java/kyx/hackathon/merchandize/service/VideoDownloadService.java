package kyx.hackathon.merchandize.service;

import kyx.hackathon.merchandize.model.VideoClip;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.UUID;

@Service
public class VideoDownloadService {

    public void downloadYoutubeVideo(VideoClip videoClip, String outputDir, UUID id) throws IOException, InterruptedException {
        File dir = new File(outputDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        ProcessBuilder processBuilder = new ProcessBuilder(
                "yt-dlp",
                "-f mp4",
                "-o", outputDir + id.toString() + ".mp4",
                videoClip.getVideoLink()
        );

        processBuilder.redirectErrorStream();

        var process = processBuilder.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }

        int exitCode = process.waitFor();
        System.out.println("yt-dlp exited with code: " + exitCode);
    }

    public void clipVideo(VideoClip videoClip, String inputFilePath, String outputFilePath) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg",
                "-i", inputFilePath,
                "-ss", videoClip.getStartTime(),
                "-t", videoClip.getDuration(),
                "-c", "copy",
                outputFilePath
        );

        pb.redirectError();

        var p = pb.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }

        int exitCode = p.waitFor();
        System.out.println("ffmpeg exited with code: " + exitCode);
    }

}
