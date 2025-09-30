package com.flexydemy.content.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QuizQuestionDTO {
    private String id;
    private String question;
    private List<String> tags;
    private List<String> options;
    private String correctAnswer;
    private int difficultyLevel;
    private boolean isAIGenerated;
    private String type;
    private String selectedAnswer;
    private boolean isCorrect;
}
