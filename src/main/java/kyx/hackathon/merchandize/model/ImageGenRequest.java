package kyx.hackathon.merchandize.model;

import lombok.Data;

import java.util.UUID;

@Data
public class ImageGenRequest {

    private UUID id;
    private String transcription;
    private String imgPath;
    private String prompt;

}