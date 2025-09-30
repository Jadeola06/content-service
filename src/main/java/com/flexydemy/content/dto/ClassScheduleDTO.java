package com.flexydemy.content.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClassScheduleDTO {
    private String id;
    private String tutorId;
    private String courseId;

    private String description;
    private Instant startTime;
    private Instant endTime;

    private boolean isLive;

    private String meetingLink;
    private String recordedVideoUrl;

    private int maximumStudents;
}
