package com.flexydemy.content.repository;

import com.flexydemy.content.model.TutorDisplaySession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface TutorDisplaySessionRepository extends JpaRepository<TutorDisplaySession, String> {
    Optional<TutorDisplaySession> findByTutor_TutorId(String tutorId);
}
