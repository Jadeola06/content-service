package com.flexydemy.content.repository;

import com.flexydemy.content.enums.Class_Categories;
import com.flexydemy.content.model.Course;
import com.flexydemy.content.model.FlashCardSet;
import com.flexydemy.content.model.Tutor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface FlashCardSetRepository extends JpaRepository<FlashCardSet, String> {
    List<FlashCardSet> findByTitleContainingIgnoreCase(String title);

    List<FlashCardSet> findByCreatedByUser(String userId);

    List<FlashCardSet> findBySubject(Class_Categories subject);
    List<FlashCardSet> findByCreatedByTutor(Tutor tutor);
    List<FlashCardSet> findByCourse(Course course);


}
