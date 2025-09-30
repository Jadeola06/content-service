package com.flexydemy.content.repository;

import com.flexydemy.content.model.LectureVideo;
import com.flexydemy.content.model.VideoProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface VideoProgressRepository extends JpaRepository<VideoProgress, String> {

    List<VideoProgress> findByStudentId(String studentId);

    List<VideoProgress> findByVideo_Id(String videoId);

    Optional<VideoProgress> findByStudentIdAndVideo(String studentId, LectureVideo video);
    Optional<VideoProgress> findByStudentIdAndVideo_Id(String studentId, String videoId);

}
