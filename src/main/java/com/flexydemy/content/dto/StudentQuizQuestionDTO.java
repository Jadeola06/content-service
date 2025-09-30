package com.flexydemy.content.dto;

import lombok.Data;

import java.util.List;

@Data
public class StudentQuizQuestionDTO {
    private String id;
    private String question;
    private List<String> tags;
    private String type;
    private List<String> options;
}
