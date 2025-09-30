package com.flexydemy.content.repository;

import com.flexydemy.content.enums.Class_Categories;
import com.flexydemy.content.model.Tutor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TutorRepository extends JpaRepository<Tutor, String> {

    // This method will search for tutors whose area of expertise contains the given subject
    Page<Tutor> findByAreaOfExpertiseContaining(Class_Categories subject, Pageable pageable);
}
