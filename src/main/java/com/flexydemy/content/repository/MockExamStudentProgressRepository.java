package com.flexydemy.content.repository;

import com.flexydemy.content.model.MockExam;
import com.flexydemy.content.model.MockExamStudentProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MockExamStudentProgressRepository extends JpaRepository<MockExamStudentProgress, String> {
    int countByMockExamAndStudentId(MockExam mockExam, String studentId);
}
