package com.flexydemy.content.repository;

import com.flexydemy.content.model.Course;
import com.flexydemy.content.model.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface LessonRepository extends JpaRepository<Lesson, String> {
    Optional<Lesson> findByCourseAndSequenceNumber(Course course, int sequenceNumber);
    int countByCourse(Course course);
    List<Lesson> findByCourse_CourseIdOrderBySequenceNumberAsc(String courseId);

    Optional<Lesson> findByQuizId(String quizId);

    List<Lesson> findByCourse(Course course);

    long countByCourse_CourseId(String courseId);
}
