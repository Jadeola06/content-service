package com.flexydemy.content.dto;

import lombok.Data;
import lombok.Getter;

import java.time.LocalDateTime;

@Data
public class UpcomingSessions {
    private String sessionId;
    private String sessionName;
    private String tutorName;
    private LocalDateTime time;
}
