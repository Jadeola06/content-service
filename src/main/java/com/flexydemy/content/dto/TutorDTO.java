package com.flexydemy.content.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.flexydemy.content.enums.Class_Categories;
import com.flexydemy.content.model.TutorDisplaySession;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TutorDTO {
    private String userId;
    private String fullName;
    private String username;
    private String email;
    private String tutorId;
    private String courseId;
    private String bio;
    private List<String> subject;
    private String profileVideoUrl;
    private boolean isVerified;
    private Long totalSubscribers;
    private String feedback;
    private int rating;
    private LocalDate joinDate;
    private int sessionCount;
    private int age;
    private String profileImageUrl;

    private String linkedinUrl;
    private String facebookUrl;
    private String xUrl;

    private List<TutorDisplaySessionDTO> availableSessions;
    private List<RatingDTO> reviews;

    private List<School> schools;

    private String resumeUrl;

    private List<Class_Categories> areaOfExpertise;
    private List<String> qualifications;

    private List<String> workingDays;
    private List<WorkExperience> workExperiences;
}
