package com.flexydemy.content.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TokenStoreRequest {
    private String accessToken;
    private String refreshToken;
    private String idToken;
    private Long expiresIn;
    private Long refreshTokenExpiresIn;
}
