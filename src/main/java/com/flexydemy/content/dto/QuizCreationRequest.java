package com.flexydemy.content.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuizCreationRequest {
    private String tutorId;
    private String courseId;
    private String lessonId;
    private String title;
    private int timeLimit;
    private List<QuizQuestionDTO> questions;
    private double passScore;

}
