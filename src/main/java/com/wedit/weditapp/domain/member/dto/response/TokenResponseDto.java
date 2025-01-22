package com.wedit.weditapp.domain.member.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
public class TokenResponseDto {
    private String accessToken;
    private String refreshToken;

    @Builder
    private TokenResponseDto(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public static TokenResponseDto from(String accessToken, String refreshToken) {
        return TokenResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
