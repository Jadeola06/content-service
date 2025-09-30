package com.flexydemy.content.dto;

import com.flexydemy.content.enums.Class_Categories;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class TutorDashboardDTO {
    private String tutorId;
    private int students;
    private int completedClasses;
    private BigDecimal earnings;

    private List<MonthlyEarningDTO> earningsByMonth;

    private List<TutorUpcomingSessions> upcomingSessions; // cap to four

    private List<TutorStudentSummaryDTO> studentsToContact; //capped at three. has their name, profile picture url, classifier //e.x "New Student", "Returning Student", exam type, ex JAMB, WAEC

    private List<TutorStudentSummaryDTO> messages; //name, profile image urls, exam , time.

    private List<RatingDTO> ratings; //capped at three. name, date of review, review, and rating.
    private Class_Categories topSubject;
    private int sessionsThisMonth;
    private double percentageComparedToLastMonth; //based on sessions

    private double averageRating; //tutor average rating


}

