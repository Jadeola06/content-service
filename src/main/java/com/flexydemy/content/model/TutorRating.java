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
@AllArgsConstructor
@NoArgsConstructor
public class TutorRating extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    @JoinColumn(name = "tutor_id", referencedColumnName = "tutor_id")
    private Tutor tutor;

    private String studentId;
    private String studentUsername;

    private int rating;
    private String feedback;
    private LocalDateTime time;
}
