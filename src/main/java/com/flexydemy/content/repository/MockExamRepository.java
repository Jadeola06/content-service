package com.flexydemy.content.repository;

import com.flexydemy.content.model.MockExam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MockExamRepository extends JpaRepository<MockExam, String> {
}
