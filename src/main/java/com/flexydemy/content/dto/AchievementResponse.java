package com.flexydemy.content.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AchievementResponse {
    private String achievement;
    private String description;
    private LocalDateTime dateAccomplished;
}
