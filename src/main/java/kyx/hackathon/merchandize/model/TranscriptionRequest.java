package kyx.hackathon.merchandize.model;

import org.springframework.core.io.Resource;
import org.springframework.lang.Nullable;
import org.springframework.web.multipart.MultipartFile;

public record TranscriptionRequest(Resource audioFile, @Nullable String context) {
    
}
