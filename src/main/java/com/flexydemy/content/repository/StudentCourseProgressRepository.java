package com.flexydemy.content.repository;

import com.flexydemy.content.model.Course;
import com.flexydemy.content.model.StudentCourseProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
@Repository
public interface StudentCourseProgressRepository extends JpaRepository<StudentCourseProgress, String>{
    Optional<StudentCourseProgress> findByUserIdAndCourse_CourseId(String userId, String courseId);
    List<StudentCourseProgress> findAllByUserId(String userId);
    List<StudentCourseProgress> findAllByCourse(Course course);
    List<StudentCourseProgress> findByUserId(String userId);
    List<StudentCourseProgress> findByCourse_Tutor_TutorId(String tutorId);

    List<StudentCourseProgress> findByUserIdAndCourse_Tutor_TutorId(String studentId, String tutorId);

    int countByCourse_CourseId(String courseId);
}
