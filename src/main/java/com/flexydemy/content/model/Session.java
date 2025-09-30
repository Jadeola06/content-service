package com.flexydemy.content.model;

import com.flexydemy.content.enums.SessionStatus;
import com.flexydemy.content.enums.SessionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Session {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String name;

    private String description;
    private String courseId;

    @ManyToOne
    @JoinColumn(name = "tutor_id", referencedColumnName = "tutor_id")
    private Tutor tutor;

    @ElementCollection
    private List<String> studentIds;

    @Enumerated(EnumType.STRING)
    private SessionType sessionType;

    @Enumerated(EnumType.STRING)
    private SessionStatus sessionStatus;

    private Duration duration;

    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private LocalDateTime scheduledTime;
    private LocalDate dateAdded;

    private boolean active;
    private boolean isLive;

    private String roomName;
}
