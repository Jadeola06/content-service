package com.flexydemy.content.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResourceFileResponse {
    private String courseId;
    private String title;
    private String Id;
    private String fileUrl;
    private String youtubeVideoId;
    private String type;
    private LocalDateTime timeUploaded;
}
