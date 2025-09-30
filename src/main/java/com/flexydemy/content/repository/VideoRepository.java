package com.flexydemy.content.repository;

import com.flexydemy.content.model.LectureVideo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface VideoRepository extends JpaRepository<LectureVideo, String> {
    Optional<LectureVideo> findByYoutubeVideoId(String videoId);
    List<LectureVideo> findAllByCourse_CourseId(String courseId);
}
