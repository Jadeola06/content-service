package com.flexydemy.content.service;

import com.flexydemy.content.dto.LectureVideoDTO;
import com.flexydemy.content.exceptions.ResourceNotFoundException;
import com.flexydemy.content.model.Course;
import com.flexydemy.content.model.LiveStream;
import com.flexydemy.content.model.LectureVideo;
import com.flexydemy.content.repository.LiveStreamRepository;
import com.flexydemy.content.repository.CourseRepository;
import com.flexydemy.content.repository.VideoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LectureVideoService {

    private static final Logger logger = LoggerFactory.getLogger(LectureVideoService.class);

    @Autowired
    private VideoRepository videoRepository;
    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private LiveStreamRepository liveStreamRepository;


    // Get video by ID
    public String getVideoById(String id) {
        logger.info("Attempting to fetch video with ID: {}", id);

        return videoRepository.findById(id)
                .map(video -> {
                    logger.info("Found video with ID: {}", id);
                    return video.getYoutubeVideoId();
                })
                .orElseThrow(() -> {
                    logger.error("Video with ID {} not found", id);
                    return new ResourceNotFoundException("Video", id);
                });
    }

    // Get all videos by Tutor ID
    public List<LectureVideoDTO> getAllVideosByCourseId(String courseId) {
        // Fetching the course to ensure it exists
        Course course = courseRepository.findById(courseId).orElseThrow(() -> {
            return new ResourceNotFoundException("Course", courseId);
        });

        // Fetching all videos associated with the course
        List<LectureVideo> videos = videoRepository.findAllByCourse_CourseId(courseId);

        // Mapping the fetched videos to LectureVideoDTO
        return videos.stream().map(video -> new LectureVideoDTO(
                video.getId(),
                video.getYoutubeVideoId(),
                video.getTitle(),
                video.getDescription(),
                video.getSubject(),
                video.getThumbnailUrl(),
                video.getUploadedBy(),
                video.getUploadedAt()
        )).toList();
    }

    public List<LiveStream> getAllLiveStreamsByCourse(String courseId) {
        logger.info("Attempting to fetch live streams for course with ID: {}", courseId);

        // Fetching the course entity to ensure it exists
        Course course = courseRepository.findById(courseId).orElseThrow(() -> {
            logger.error("Course with ID {} not found", courseId);
            return new ResourceNotFoundException("Course", courseId);
        });

        // Fetching all live streams related to the course
        List<LiveStream> liveStreams = liveStreamRepository.findAllByCourse_CourseId(courseId);

        if (liveStreams.isEmpty()) {
            logger.info("No live streams found for course with ID: {}", courseId);
        } else {
            logger.info("Found {} live streams for course with ID: {}", liveStreams.size(), courseId);
        }

        return liveStreams;
    }


    // Delete video by ID
    @Transactional
    public String deleteVideo(String videoId) {
        logger.info("Attempting to delete video with ID: {}", videoId);

        if (!videoRepository.existsById(videoId)) {
            logger.error("Video with ID {} not found for deletion", videoId);
            throw new ResourceNotFoundException("Video", videoId);
        }

        videoRepository.deleteById(videoId);
        logger.info("Video with ID {} successfully deleted", videoId);
        return "Video Successfully Deleted";
    }
}
