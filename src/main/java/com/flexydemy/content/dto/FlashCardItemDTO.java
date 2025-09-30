package com.flexydemy.content.dto;

import lombok.Data;

import java.util.List;


@Data
public class FlashCardItemDTO {
    private String question;
    private String correctAnswer;
    private List<String> options;
}