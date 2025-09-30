package com.flexydemy.content.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CourseLiveStreamDTO {
    private String id;
    private String youtubeBroadcastId;
    private String youtubeStreamId;
    private String ingestionAddress;
    private String streamKey;
    private String watchUrl;
    private String title;
    private String description;
    private String scheduledTime;
}
