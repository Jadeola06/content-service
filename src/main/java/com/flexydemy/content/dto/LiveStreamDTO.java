package com.flexydemy.content.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LiveStreamDTO {
    String title;
    String description;
    String startDateTime;
    String endDateTime;
    String courseId;
    String tutorId;
}
