package com.flexydemy.content.model;


import com.flexydemy.content.dto.UserDto;
import com.flexydemy.content.enums.Class_Categories;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Course extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "course_id")
    private String courseId;

    private String courseTitle;
    private String description;

    private Class_Categories subject;

    private String gradeLevel;
    private boolean isPublished;
    private String duration;

    @Transient
    private List<UserDto> enrolledStudents;

    @ManyToOne
    @JoinColumn(name = "tutor_id", referencedColumnName = "tutor_id")
    private Tutor tutor;

    private int ratingsCount;
    private double averageRating;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Lesson> lessons;


    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StudentCourseProgress> enrolledUsers;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LiveStream> liveStreams;

    public List<String> getEnrolledStudents() {
        if (enrolledUsers == null) return List.of();
        return enrolledUsers.stream()
                .map(StudentCourseProgress::getUserId)
                .toList();
    }

    public Course(String courseTitle, String description, Class_Categories subject,
                  String gradeLevel, boolean isPublished,
                  String duration, int ratingsCount, double averageRating, Tutor tutor) {
        this.courseTitle = courseTitle;
        this.description = description;
        this.subject = subject;
        this.gradeLevel = gradeLevel;
        this.isPublished = isPublished;
        this.duration = duration;
        this.ratingsCount = ratingsCount;
        this.averageRating = averageRating;
        this.tutor = tutor;
    }


}
