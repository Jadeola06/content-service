package com.flexydemy.content.dto;

import lombok.Data;

import java.util.List;
@Data
public class StudentQuizDTO {
    private String id;
    private String title;
    private int totalQuestions;
    private List<StudentQuizQuestionDTO> questions;
}
