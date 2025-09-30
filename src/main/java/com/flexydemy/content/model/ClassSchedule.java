package com.flexydemy.content.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ClassSchedule extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    @JoinColumn(name = "tutor_id", referencedColumnName = "tutor_id")
    private Tutor tutor;

    @ManyToOne
    @JoinColumn(name = "course_id", referencedColumnName = "course_id")
    private Course course;

    @ElementCollection
    @CollectionTable(name = "class_schedule_students", joinColumns = @JoinColumn(name = "schedule_id"))
    @Column(name = "student_id")
    private List<String> studentIds;

    private String description;
    private Instant startTime;
    private Instant endTime;
    private boolean isLive;
    private String meetingLink;
    private String recordedVideoUrl;
    private int maximumStudents;
}
