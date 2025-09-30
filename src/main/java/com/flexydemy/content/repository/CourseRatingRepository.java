package com.flexydemy.content.repository;

import com.flexydemy.content.model.Course;
import com.flexydemy.content.model.CourseRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface CourseRatingRepository extends JpaRepository<CourseRating, String> {
    List<CourseRating> findByCourse_CourseId(String courseId);

    boolean existsByStudentIdAndCourse(String studentId, Course course);
}
