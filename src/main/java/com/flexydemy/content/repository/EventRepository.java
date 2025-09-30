package com.flexydemy.content.repository;

import com.flexydemy.content.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;
@Repository
public interface EventRepository extends JpaRepository<Event, String> {
    List<Event> findByStudentId(String studentId);

    List<Event> findByTutor_TutorId(String tutorId);
}
