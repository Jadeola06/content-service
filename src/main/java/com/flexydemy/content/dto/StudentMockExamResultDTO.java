package com.flexydemy.content.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentMockExamResultDTO {
    private String mockExamId;
    private String studentId;
    private int totalQuestions;
    private String title;
    private int correctAnswers;
    private double scorePercentage;
    private List<MockExamQuestionResult> questions;
}
