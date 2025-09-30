package com.flexydemy.content.model;


import com.flexydemy.content.enums.ExamType;
import com.flexydemy.content.enums.SessionStatus;
import lombok.Data;

import java.time.Duration;
import java.time.LocalDateTime;

@Data
public class TutorSessionStudentDTO {
    private String studentId;
    private String studentName;
    private String email;
    private String phoneNumber;
    private ExamType examType;
    private String courseTitle; // optional
    private Duration sessionDuration;
    private SessionStatus sessionStatus;
    private LocalDateTime scheduledDate;
}

