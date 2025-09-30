package com.flexydemy.content.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MockExamQuestionResult {
    private String questionId;
    private String selectedAnswer;
    private String correctAnswer;
    private boolean isCorrect;
}
