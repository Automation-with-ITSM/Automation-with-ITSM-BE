package com.wedit.weditapp.global.auth.login.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RedisTemplate<String, String> redisTemplate;

    @Value("${jwt.refresh.expiration}")
    private long refreshTokenExpiry;

    // 사용자 이메일로 Refresh Token 저장
    public void saveRefreshToken(String email, String refreshToken) {
        redisTemplate.opsForValue()
                .set(email, refreshToken, refreshTokenExpiry, TimeUnit.MILLISECONDS);
    }

    // 사용자 이메일로 저장된 Refresh Token 조회
    public String getRefreshToken(String email) {
        return redisTemplate.opsForValue().get(email);
    }

    // 사용자 이메일로 저장된 Refresh Token 삭제
    public void deleteRefreshToken(String email) {
        redisTemplate.delete(email);
    }

}
