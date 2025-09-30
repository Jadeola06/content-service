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
public class ClassAttendanceDTO {
    private String studentId;
    private boolean attended;
    private Instant attendanceTime;
}
