package com.flexydemy.content.dto;

import com.flexydemy.content.enums.Class_Categories;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LectureVideoDTO {
    private String id;
    private String youtubeVideoId;
    private String title;
    private String description;
    private Class_Categories subject;
    private String thumbnailUrl;
    private String uploadedBy;
    private LocalDateTime uploadedAt;
}
