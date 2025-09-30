package com.flexydemy.content.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class DashboardProgressDTO {
    private int classCount;
    private List<String> classes;
}
