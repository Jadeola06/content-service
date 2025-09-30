package com.flexydemy.content.model;

import com.flexydemy.content.dto.UserDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class MockExamStudentProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(optional = false)
    private MockExam mockExam;

    private String studentId;

    private int attemptNumber;

    private double score;

    private LocalDateTime attemptDate;
}