package com.flexydemy.content.dto;

import lombok.Data;

@Data
public class CustomSessionRequestDTO {
    private String studentId;
    private String tutorId;
    private int durationMinutes;
    private String startDateTime;
}
