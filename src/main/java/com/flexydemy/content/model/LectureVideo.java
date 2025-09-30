package com.flexydemy.content.model;

import com.flexydemy.content.enums.Class_Categories;
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
@Table(name = "videos")
public class LectureVideo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String youtubeVideoId;
    private String youtubeVideoUrl;
    private String title;
    private String description;
    private Class_Categories subject;
    private String thumbnailUrl;
    private String uploadedBy;

    private LocalDateTime uploadedAt = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "tutor_id", referencedColumnName = "tutor_id", nullable = false)
    private Tutor tutor;

    @ManyToOne
    @JoinColumn(name = "course_id", referencedColumnName = "course_id", nullable = false)
    private Course course;
}
