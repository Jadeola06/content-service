package com.flexydemy.content.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuizSubmitRequest {
    private String quizId;
    private String studentId;
    private List<QuizQuestionSubmissionDTO> answers;
}
