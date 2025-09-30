package com.flexydemy.content.dto;

import com.flexydemy.content.model.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Duration;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EventDTO {
    private String id;
    private String sessionId;
    private String studentId;
    private String tutorId;
    private int durationInMinutes;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public EventDTO(Event event) {
        this.id = event.getId();
        this.sessionId = event.getSessionId();
        this.studentId = event.getStudentId();
        this.tutorId = event.getTutor() != null ? event.getTutor().getTutorId() : null;
        this.durationInMinutes = event.getDuration().toMinutesPart();
        this.startTime = event.getStartTime();
        this.endTime = event.getEndTime();
    }
}