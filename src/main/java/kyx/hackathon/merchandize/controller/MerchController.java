package kyx.hackathon.merchandize.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.UUID;

import kyx.hackathon.merchandize.model.VideoClip;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MerchController {

  @PostMapping("/merch")
  public void merch(@RequestBody VideoClip videoClip) throws Exception {
    UUID id = UUID.randomUUID();
    String outputDir = "/home/wyatthlang/Downloads/";

    if ("YOUTUBE".equals(videoClip.getVideoType())) {
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

    var inputFilePath = outputDir + id.toString() + ".mp4";
    var outputFilePath = outputDir + id.toString() + "-clip" + ".mp4";

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
