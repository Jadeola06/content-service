package com.flexydemy.content.enums;


public enum AchievementTypes {

    FIRST_LOGIN("First Login", "Logged into your account for the first time."),
    COMPLETE_FIRST_LESSON("Lesson Beginner", "Completed your first lesson."),
    COMPLETE_FIRST_COURSE("Course Finisher", "Successfully completed your first course."),
    WEEKLY_STREAK_3("3-Day Streak", "Logged in and studied for 3 consecutive days."),
    WEEKLY_STREAK_7("7-Day Streak", "Kept a study streak for a whole week."),
    EARLY_BIRD("Early Bird", "Completed a lesson before 8 AM."),
    NIGHT_OWL("Night Owl", "Completed a lesson after 10 PM."),
    QUIZ_MASTER("Quiz Master", "Scored over 90% on 3 quizzes in a row."),
    PERFECTIONIST("Perfectionist", "Achieved 100% in a course."),
    COMMUNITY_HELPER("Community Helper", "Answered a question or helped a peer."),
    FAST_TRACK("Fast Track", "Completed a course in under 3 days."),
    MONTHLY_HERO("Monthly Hero", "Logged in every day for a month.");

    private final String name;
    private final String description;

    AchievementTypes(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
