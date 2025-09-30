package com.flexydemy.content.model;

import com.flexydemy.content.enums.LessonType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "lessons")
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String title;

    @Column(length = 5000)
    private String about; // For text-based lessons (can be HTML or markdown)

    private String transcript;
    private String notes;

    @Enumerated(EnumType.STRING)
    private LessonType lessonType; // VIDEO or TEXT

    private int sequenceNumber; // To enforce order in Course

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    private String duration; // in Minutes

    private String quizId;
    private double passingScore;

    private boolean quizNeeded;
    private int quizTimeLimit;
    private int questionCount;

    @OneToOne
    @JoinColumn(name = "video_id")
    private LectureVideo video; // Optional: Only for video lessons

    private double averageRating;
}
