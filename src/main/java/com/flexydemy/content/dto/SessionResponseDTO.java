package com.flexydemy.content.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.flexydemy.content.enums.SessionType;
import com.flexydemy.content.exceptions.BadRequestException;
import com.flexydemy.content.model.Session;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SessionResponseDTO {
    private String sessionId;
    private String name;
    private boolean active;
    private boolean isLive;
    private Long duration;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private String type;
    private int rating;
    private String phone;
    private String status;
    private LocalDate addedDate;
    private List<SessionStudent> students;
    private SessionStudent tutor;
    private String roomName;
    private String meetingToken;



    public SessionResponseDTO(Session session, boolean isTutor, String token) {
        this.sessionId = session.getId();
        this.name = session.getName();
        this.active = session.isActive();
        this.isLive = session.isLive();
        this.duration = session.getDuration().toMinutes();
        this.startDateTime = session.getStartDateTime();
        this.endDateTime = session.getEndDateTime();
        this.type = session.getSessionType()  == SessionType.GROUP ? "Group Session" : "1-On-1";
        this.phone = "phoneNumber";
        this.addedDate = session.getDateAdded();

        LocalDateTime now = LocalDateTime.now();
        if (!session.isActive()) {
            this.status = "Cancelled";
        } else if (session.getScheduledTime() != null && session.getScheduledTime().isBefore(now)) {
            this.status = "Completed";
        } else {
            this.status = "Active";
        }



        if (token == null || token.isEmpty() || token.isBlank()){
            this.meetingToken = null;
        }else{
            this.meetingToken =  token;
        }
        this.roomName = session.getRoomName();


    }
    private int toDurationMinutes(Duration duration) {
        if (duration == null || duration.isZero() || duration.isNegative()) {
            throw new BadRequestException("Invalid duration.");
        }
        return (int) duration.toMinutes();
    }
}