package com.flexydemy.content.repository;

import com.flexydemy.content.model.ResourceDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResourceDocumentRepository extends JpaRepository<ResourceDocument, String> {
    List<ResourceDocument> findByCourseId(String courseId);

    List<ResourceDocument> findByLessonId(String lessonId);
}
