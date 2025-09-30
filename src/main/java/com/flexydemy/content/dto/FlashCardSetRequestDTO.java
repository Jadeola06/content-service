package com.flexydemy.content.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class FlashCardSetRequestDTO {
    private String title;
    private String courseId;
    private String lessonId;
    private String tutorId;
    private String classCategory;
    private List<FlashCardItemDTO> flashCards;
}
