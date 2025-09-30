package com.flexydemy.content.dto;

import lombok.Data;

import java.util.List;

@Data
public class SessionDashboard {
    private List<SessionResponseDTO> upcomingSessions;
    private List<SimpleTutorDTO> availableTutors;
    private List<SessionResponseDTO> recentSessions;
}
