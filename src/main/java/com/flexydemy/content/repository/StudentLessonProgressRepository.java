package com.flexydemy.content.repository;

import com.flexydemy.content.model.Lesson;
import com.flexydemy.content.model.StudentCourseProgress;
import com.flexydemy.content.model.StudentLessonProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
@Repository
public interface StudentLessonProgressRepository extends JpaRepository<StudentLessonProgress, String> {
    Optional<StudentLessonProgress> findByCourseProgressAndLesson(StudentCourseProgress courseProgress, Lesson lesson);
    int countByCourseProgressAndCompletedTrue(StudentCourseProgress courseProgress);

    List<StudentLessonProgress> findByCourseProgress_UserId(String userId);

    List<StudentLessonProgress> findByCourseProgress_UserIdAndCompletedAtAfter(String userId, LocalDateTime after);

}
