package com.flexydemy.content.model;


import com.flexydemy.content.dto.School;
import com.flexydemy.content.dto.WorkExperience;
import com.flexydemy.content.enums.Class_Categories;
import com.flexydemy.content.enums.TutorStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Tutor {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "tutor_id")
    private String tutorId;

    private String userId;



    private String bio;

    @ElementCollection
    @CollectionTable(name = "tutor_schools", joinColumns = @JoinColumn(name = "tutor_id"))
    private List<School> schools;

    @ElementCollection
    @CollectionTable(name = "tutor_experience", joinColumns = @JoinColumn(name = "tutor_id"))
    private List<WorkExperience> workExperiences;

    private String gender;



    @ElementCollection
    @CollectionTable(name = "tutor_working_days", joinColumns = @JoinColumn(name = "tutor_id"))
    @Column(name = "day")
    private List<String> workingDays;

    private String profileImageUrl;


    @ElementCollection
    @Enumerated(EnumType.STRING)
    private List<Class_Categories> areaOfExpertise;

    @Enumerated(EnumType.STRING)
    private TutorStatus status;

    @OneToMany(mappedBy = "tutor")
    private List<LectureVideo> allLectureVideos;

    @OneToMany(mappedBy = "tutor", cascade = CascadeType.ALL)
    private List<Course> courses;

    @ElementCollection
    @CollectionTable(name = "tutor_qualifications", joinColumns = @JoinColumn(name = "tutor_id"))
    @Column(name = "qualifications")
    private List<String> qualifications;

    private String profileVideoUrl;
    private boolean isVerified;
    private Long totalSubscribers;
    private int ratingsCount;
    private double averageRating;
    private LocalDate joinDate;

    private boolean resumeCollected;
    private String resumeUrl;

    private String linkedinUrl;
    private String facebookUrl;
    private String xUrl;

}
