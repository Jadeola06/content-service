package com.flexydemy.content.service;

import com.flexydemy.content.dto.VideoProgressDTO;
import com.flexydemy.content.exceptions.ResourceNotFoundException;
import com.flexydemy.content.model.LectureVideo;
import com.flexydemy.content.dto.UserDto;
import com.flexydemy.content.model.VideoProgress;
import com.flexydemy.content.repository.clients.UserClient;
import com.flexydemy.content.repository.VideoProgressRepository;
import com.flexydemy.content.repository.VideoRepository;
import com.flexydemy.content.utils.Utils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Optional;

@Service
public class VideoProgressService {

    private final VideoProgressRepository videoProgressRepository;
    private final UserClient userRepository;
    private final VideoRepository videoRepository;

    @Autowired
    public VideoProgressService(VideoProgressRepository videoProgressRepository,
                                UserClient userRepository,
                                VideoRepository videoRepository) {
        this.videoProgressRepository = videoProgressRepository;
        this.userRepository = userRepository;
        this.videoRepository = videoRepository;
    }

    public VideoProgressDTO updateProgress(VideoProgressDTO dto, HttpServletRequest request) {

        String token = Utils.getToken(request);
        UserDto student = userRepository.findById(dto.getStudentId(), token)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Student not found")))
                .block();

        LectureVideo video = videoRepository.findById(dto.getVideoId())
                .orElseThrow(() -> new ResourceNotFoundException("Lecture Video", dto.getVideoId()));

        VideoProgress videoProgress = videoProgressRepository
                .findByStudentIdAndVideo(student.getUserId(), video)
                .orElse(new VideoProgress());

        videoProgress.setStudentId(student.getUserId());
        videoProgress.setStudentUserName(student.getUsername());
        videoProgress.setVideo(video);
        videoProgress.setSecondsWatched(dto.getSecondsWatched());
        videoProgress.setLastWatchedAt(Instant.now());
        videoProgress.setCompleted(dto.isCompleted());

        VideoProgress saved = videoProgressRepository.save(videoProgress);

        return toResponseDTO(saved);
    }

    public VideoProgressDTO getVideoProgressByStudent(VideoProgressDTO videoProgressDTO , HttpServletRequest request) {
        String token = Utils.getToken(request);

        UserDto student = userRepository.findById(videoProgressDTO.getStudentId(), token)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Student not found")))
                .block();

        LectureVideo lectureVideo = videoRepository.findById(videoProgressDTO.getVideoId())
                .orElseThrow(() -> new ResourceNotFoundException("Lecture Video", videoProgressDTO.getVideoId()));

        Optional<VideoProgress> progress = videoProgressRepository.findByStudentIdAndVideo(student.getUserId(), lectureVideo);
        return progress.map(this::toResponseDTO).orElse(null);
    }

    private VideoProgressDTO toResponseDTO(VideoProgress progress) {
        VideoProgressDTO dto = new VideoProgressDTO();
        dto.setStudentId(progress.getStudentId());
        dto.setVideoId(progress.getVideo().getId());
        dto.setSecondsWatched(progress.getSecondsWatched());
        dto.setLastWatchedAt(progress.getLastWatchedAt());
        dto.setCompleted(progress.isCompleted());
        return dto;
    }
}
