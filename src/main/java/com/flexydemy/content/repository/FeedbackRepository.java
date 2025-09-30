package com.flexydemy.content.repository;

import com.flexydemy.content.model.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, String> {
    int countByReviewedFalse();
}
