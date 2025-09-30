package com.flexydemy.content.dto;

import lombok.Data;

import java.util.List;


@Data
public class StudentQuizResultDTO {
    private String quizId;
    private String title;
    private int totalQuestions;
    private int correctAnswers;
    private double scorePercentage;
    private List<QuizQuestionResultDTO> answers;
}
