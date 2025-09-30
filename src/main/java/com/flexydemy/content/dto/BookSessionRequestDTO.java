package com.flexydemy.content.dto;

import lombok.Data;

@Data
public class BookSessionRequestDTO {
    private String studentId;
    private String tutorId;
    private String displaySessionId;
    private long durationInMinutes;
    private String startTime;
}
