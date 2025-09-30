package com.flexydemy.content.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class JoinLiveSessionResponse {
    private String studentId;
    private String watchUrl;
    private String sessionName;
    private String sessionDescription;
}
