package com.flexydemy.content.repository;

import com.flexydemy.content.model.ClassSchedule;
import com.flexydemy.content.model.Course;
import com.flexydemy.content.model.Tutor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface ClassScheduleRepository extends JpaRepository<ClassSchedule, String> {
    List<ClassSchedule> findByTutor(Tutor tutor);

    List<ClassSchedule> findByStudentIdsContaining(String studentId);

    List<ClassSchedule> findByCourse(Course course);
}
