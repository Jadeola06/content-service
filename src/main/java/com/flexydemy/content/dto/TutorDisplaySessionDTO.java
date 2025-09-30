package com.flexydemy.content.dto;

import com.flexydemy.content.model.TutorDisplaySession;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TutorDisplaySessionDTO {

    private String id;
    private String name;
    private String description;
    private long durationMinutes;
    private String tutorId;

    public TutorDisplaySessionDTO(TutorDisplaySession entity) {
        this.id = entity.getId();
        this.name = entity.getName();
        this.description = entity.getDescription();
        this.durationMinutes = entity.getDuration().toMinutes();
        this.tutorId = entity.getTutor() != null ? entity.getTutor().getTutorId() : null;
    }
}