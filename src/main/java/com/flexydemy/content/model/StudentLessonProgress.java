package com.flexydemy.content.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StudentLessonProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    private Lesson lesson;

    @ManyToOne
    private StudentCourseProgress courseProgress;

    private boolean completed;          // Did they view/finish the lesson?
    private boolean passedQuiz;         // Did they pass the quiz?
    private double quizScore;
    private LocalDateTime completedAt;
}
