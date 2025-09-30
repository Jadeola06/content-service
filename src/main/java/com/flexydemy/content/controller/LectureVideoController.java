package com.flexydemy.content.controller;

import com.flexydemy.content.dto.LectureVideoDTO;
import com.flexydemy.content.exceptions.ResourceNotFoundException;
import com.flexydemy.content.model.LiveStream;
import com.flexydemy.content.service.LectureVideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/videos")
public class LectureVideoController {

    private final LectureVideoService lectureVideoService;

    @Autowired
    public LectureVideoController(LectureVideoService lectureVideoService) {
        this.lectureVideoService = lectureVideoService;
    }

    // Endpoint to get video by ID
    @GetMapping("/{id}")
    public ResponseEntity<String> getVideoById(@PathVariable("id") String id) {
        try {
            String youtubeVideoId = lectureVideoService.getVideoById(id);
            return new ResponseEntity<>(youtubeVideoId, HttpStatus.OK);
        } catch (ResourceNotFoundException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    // Endpoint to get all videos by course ID
    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<LectureVideoDTO>> getAllVideosByCourseId(@PathVariable("courseId") String courseId) {
        try {
            List<LectureVideoDTO> videos = lectureVideoService.getAllVideosByCourseId(courseId);
            return new ResponseEntity<>(videos, HttpStatus.OK);
        } catch (ResourceNotFoundException ex) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    // Endpoint to get all live streams by course ID
    @GetMapping("/course/{courseId}/live-streams")
    public ResponseEntity<List<LiveStream>> getAllLiveStreamsByCourse(@PathVariable("courseId") String courseId) {
        try {
            List<LiveStream> liveStreams = lectureVideoService.getAllLiveStreamsByCourse(courseId);
            return new ResponseEntity<>(liveStreams, HttpStatus.OK);
        } catch (ResourceNotFoundException ex) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{videoId}")
    public ResponseEntity<String> deleteVideo(@PathVariable("videoId") String videoId) {
        try {
            String response = lectureVideoService.deleteVideo(videoId);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (ResourceNotFoundException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
}
