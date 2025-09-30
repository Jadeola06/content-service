package com.flexydemy.content.dto;

import lombok.Data;

import java.util.List;

@Data
public class FlashCardSetResponseDTO {
    private String id;
    private String title;
    private String courseTitle;
    private String lessonTitle;
    private String createdBy;
    private String subject;
    private int flashCardsCount;
    private List<FlashCardItemDTO> flashCards;
}
