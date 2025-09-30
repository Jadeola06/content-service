package com.flexydemy.content.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VideoProgress extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String studentId;
    private String studentUserName;

    @OneToOne
    @JoinColumn(name = "video_id", referencedColumnName = "id")
    private LectureVideo video;

    private int secondsWatched;
    private Instant lastWatchedAt;
    private boolean isCompleted;

}
