package com.flexydemy.content.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class LessonDTO {
    private String lessonId;
    private String title;
    private String about;
    private String transcript;
    private String notes;
    private String youtubeVideoId;
    private int sequenceNumber;
    private double passingScore;
    private String courseId;
    private String quizId;
    private boolean quizNeeded;
    private int quizTimeLimit;
    private String videoUrl;

    private double averageRating;

    private Object quiz;
    private List<ResourceFileResponse> resourceFiles;

}
