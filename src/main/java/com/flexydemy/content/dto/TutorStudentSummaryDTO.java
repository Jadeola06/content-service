package com.flexydemy.content.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.flexydemy.content.enums.Class_Categories;
import com.flexydemy.content.enums.ExamType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TutorStudentSummaryDTO {
    private String studentId;
    private String studentName;
    private String profilePictureUrl;
    private String courseId;
    private String courseTitle;
    private String classifier;
    private Class_Categories courseCategory;
    private double progressPercentage;
    private int sessionCountWithTutor;
    private Double rating;
    private LocalDateTime time;
    private ExamType exam;
    private LocalDateTime lastMessageTime;
}