package com.flexydemy.content.repository;

import com.flexydemy.content.model.Tutor;
import com.flexydemy.content.model.TutorRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface TutorRatingRepository extends JpaRepository<TutorRating,String> {
    List<TutorRating> findByTutor(Tutor tutor);
    List<TutorRating> findByTutor_TutorId(String id);

    List<TutorRating> findTop3ByTutor_TutorIdOrderByTimeDesc(String tutorId);

    boolean existsByStudentIdAndTutor(String studentId, Tutor tutor);
}
