package com.flexydemy.content.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class AdminSessionDisplayDTO {
    private String tutorName;
    private String studentName;
    private String courseTitle;
    private int progressPercentage;
    private String status;
    private LocalDateTime date;
}