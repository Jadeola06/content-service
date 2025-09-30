package com.flexydemy.content.dto;

import com.google.api.services.youtube.YouTube;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class MaterialsDTO {
    private List<ResourceFileResponse> videos = new ArrayList<>();
    private List<ResourceFileResponse> notes = new ArrayList<>();
    private List<ResourceFileResponse> images = new ArrayList<>();
    private List<ResourceFileResponse> documents = new ArrayList<>();
}
