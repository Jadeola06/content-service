package com.flexydemy.content.dto;

import lombok.Data;

@Data
public class TutorCourseAnalyticsDTO {
    private String courseId;
    private String courseTitle;
    private int studentCount;
    private double averageRating;
    private int sessionCount;
    private double percentageChangeFromLastMonth;
    private String status; // “High”, “Steady”, “Low”
}
