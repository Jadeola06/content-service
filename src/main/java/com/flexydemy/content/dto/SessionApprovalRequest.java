package com.flexydemy.content.dto;

import lombok.Data;

import java.time.Duration;
import java.time.LocalDateTime;

@Data
public class SessionApprovalRequest {
    private String sessionId;
    private String tutorId;
    private Duration duration;
    private LocalDateTime startTime;
}
