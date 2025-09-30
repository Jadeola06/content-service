package com.flexydemy.content.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class VideoProgressDTO {
    private String videoId;
    private String studentId;
    private String studentUsername;
    private int secondsWatched;
    private Instant lastWatchedAt;
    private boolean isCompleted;
}
