package com.flexydemy.content.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QuizResponseDTO {
    private String id; // ✅ Quiz ID — this is what course service will store
    private String studentId; // Creator or assigned user
    private String title;
    private int totalQuestions;
    private int correctAnswers;
    private double scorePercentage;
    private List<QuizQuestionDTO> questions; // Optional, used mostly when viewing or submitting
}
