package com.flexydemy.content.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LiveStream {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String youtubeBroadcastId;
    private String youtubeStreamId;
    private String ingestionAddress;
    private String streamKey;
    private String watchUrl;

    private String title;
    private String description;
    private String scheduledTime;

    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;
}
