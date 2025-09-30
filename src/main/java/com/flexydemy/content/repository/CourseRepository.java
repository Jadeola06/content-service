package com.flexydemy.content.repository;

import com.flexydemy.content.dto.TopCourseDTO;
import com.flexydemy.content.enums.Class_Categories;
import com.flexydemy.content.model.Course;
import com.flexydemy.content.model.Tutor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface CourseRepository extends JpaRepository<Course, String> {
    List<Course> findByTutor(Tutor tutor);

    List<Course> findBySubject(Class_Categories subject);

    List<Course> findByTutor_TutorId(String tutorId);

}
