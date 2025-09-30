package com.flexydemy.content.dto;

import lombok.Data;

import java.util.List;

@Data
public class TeacherQuizDTO {
    private String id;
    private String title;
    private int totalQuestions;
    private double scorePercentage;
    private List<QuizQuestionDTO> questions;
}
