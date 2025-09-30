package com.flexydemy.content.dto;

import com.flexydemy.content.enums.Class_Categories;
import lombok.Data;

import java.util.List;

@Data
public class UpdateTutorDTO {
    private String userId;
    private String tutorId;

    private String bio;

    private List<School> schools;

    private String resumeUrl;

    private List<Class_Categories> areaOfExpertise;
    private List<WorkExperience> workExperiences;
    private List<String> qualifications;

    private List<String> workingDays;

    private String linkedinUrl;
    private String facebookUrl;
    private String xUrl;

}
