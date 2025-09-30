package com.flexydemy.content.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class StudentDashboardDTO {
    private String studentId;
    private DashboardProgressDTO notStarted;
    private DashboardProgressDTO classesInProgress;
    private DashboardProgressDTO classesCompleted;
    private DashboardProgressDTO totalClasses;

    private String continueCourseId;
    private String quizId;

    private List<UpcomingSessions> upcomingSessions;

    private List<ContinueLearning> continueLearnings;

    private List<LearningTimeByMonthDTO> learningTimesByMonth;

    private List<AchievementResponse> recentAchievements;

    private LearningAnalytics learningAnalytics;

}

