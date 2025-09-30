package com.flexydemy.content.dto;

import com.flexydemy.content.enums.SessionType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TutorUpcomingSessions {
    private String sessionId;
    private String name;
    private LocalDateTime time;
    private int studentCount; // if type is group
    private SessionType sessionType;
}
