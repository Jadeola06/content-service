package com.flexydemy.content.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LessonIntroDTO {
    private String lessonId;
    private String title;
    private int lessonCount;
    private String description;
    private String durationInMinutes;
}
