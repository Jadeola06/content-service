package com.flexydemy.content.dto;

import lombok.Data;

@Data
public class QuizQuestionResultDTO {
    private String questionId;
    private String selectedAnswer;
    private String correctAnswer;
    private boolean isCorrect;
}
