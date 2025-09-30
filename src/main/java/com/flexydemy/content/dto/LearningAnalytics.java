package com.flexydemy.content.dto;

import lombok.Data;

@Data
public class LearningAnalytics {
    private int studyTimeThisWeekInMinutes;
    private double quizAccuracy;
    private int rankInClass;
    private int daysActive;
}
