package com.flexydemy.content.model;

import com.flexydemy.content.enums.Class_Categories;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FlashCardSet {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String title;

    @ManyToOne(optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne
    @JoinColumn(name = "lesson_id")
    private Lesson lesson;

    @Enumerated(EnumType.STRING)
    private Class_Categories subject;


    @ManyToOne
    @JoinColumn(name = "tutor_id")
    private Tutor createdByTutor;

    private String createdByUser;
    private String updatedBy;

    @OneToMany(mappedBy = "flashCardSet", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FlashCard> flashCards;

}
