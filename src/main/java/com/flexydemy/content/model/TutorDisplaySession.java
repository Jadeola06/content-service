package com.flexydemy.content.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Duration;


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TutorDisplaySession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String name;
    private String description;

    private Duration duration;

    @ManyToOne
    @JoinColumn(name = "tutor_id")
    private Tutor tutor;

    // subject area, skill level, etc.
}