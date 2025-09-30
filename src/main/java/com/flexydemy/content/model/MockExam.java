package com.flexydemy.content.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class MockExam {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String mockExamId;

    @ElementCollection
    private List<String> subject;

    private String title;
    private int questionCount;

    private int timeLimit;
}
