package com.flexydemy.content.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "student_course_progress")
public class StudentCourseProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String userId;

    private String username;

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    private int currentLessonSequence; // ← This tracks where they are now


    @Column(name = "progress_percentage", nullable = false)
    private double progressPercentage;

    private boolean isCompleted;

    private boolean fiftyPercentNotificationSent;
    private boolean eightyPercentNotificationSent;

    private LocalDateTime completedAt;

    @OneToMany(mappedBy = "courseProgress", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<StudentLessonProgress> lessonProgressList = new ArrayList<>();

}
