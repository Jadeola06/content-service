package com.flexydemy.content.controller;


import com.flexydemy.content.service.YouTubeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.util.Map;

@RestController
@RequestMapping("/api/v1/youtube")
public class YoutubeController {

    private final YouTubeService youTubeService;


    @Autowired
    public YoutubeController(YouTubeService youTubeService) {
        this.youTubeService = youTubeService;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadVideo(
            @RequestParam MultipartFile file,
            @RequestParam String title,
            @RequestParam(required = false) String description,
            @RequestParam String tutorId,
            @RequestParam String lessonId
    ) {
        System.out.println(title);
        System.out.println(description);
        try {
            String youtubeId = youTubeService.uploadVideo(file, title, description, tutorId, lessonId);
            return ResponseEntity.ok(Map.of("youtubeVideoId", youtubeId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }



}
