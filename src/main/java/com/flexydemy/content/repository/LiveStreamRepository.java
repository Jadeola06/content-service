package com.flexydemy.content.repository;

import com.flexydemy.content.model.LiveStream;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface LiveStreamRepository extends JpaRepository<LiveStream, String> {
    List<LiveStream> findByCourse_CourseIdOrderByScheduledTimeDesc(String courseId);
    List<LiveStream> findAllByCourse_CourseId(String courseId);
}
