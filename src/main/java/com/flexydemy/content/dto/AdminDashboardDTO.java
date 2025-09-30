package com.flexydemy.content.dto;

import lombok.Data;

import java.util.List;

@Data
public class AdminDashboardDTO {
    private String userId;
    private int students;
    private int teachers;
    private int oneOnOneSessions;
    private int feedbacks;

    // Earnings graph (monthly breakdown)
    private List<AdminMonthlyEarningBreakdown> earningsByMonth; // JAMB vs WAEC

    // Subject demand (e.g., number of students per subject)
    private List<SubjectDemandDTO> subjectDemand;

    // List of all sessions with relevant info
    private List<AdminSessionDisplayDTO> sessions;

    // Top performing courses
    private List<TopCourseDTO> topCourses;

    // Demand per class category (category vs student count)
    private List<ClassCategoryDemandDTO> categoryDemand;

}
