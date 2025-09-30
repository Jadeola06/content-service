package com.flexydemy.content.dto;

import lombok.Data;

import java.util.List;
@Data
public class TutorSessionCreateRequestDTO {
    private String tutorId;
    private List<String> studentIds;
    private String name;
    private String sessionType; // "ONE_ON_ONE" or "GROUP"
    private String startDateTime; // ISO format
    private int durationMinutes;
}
