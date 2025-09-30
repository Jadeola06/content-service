package com.flexydemy.content.controller;

import com.flexydemy.content.dto.VideoProgressDTO;
import com.flexydemy.content.service.VideoProgressService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/progress")
public class VideoProgressController {

    @Autowired
    private VideoProgressService videoProgressService;

    @PostMapping("/update")
    public ResponseEntity<?> updateProgress(@RequestBody VideoProgressDTO dto, HttpServletRequest request) {
        return ResponseEntity.ok(videoProgressService.updateProgress(dto, request));
    }

    @GetMapping("/")
    public ResponseEntity<?> getProgress(@RequestBody VideoProgressDTO videoProgressDTO, HttpServletRequest request) {
        return ResponseEntity.ok(videoProgressService.getVideoProgressByStudent(videoProgressDTO, request));
    }
}
