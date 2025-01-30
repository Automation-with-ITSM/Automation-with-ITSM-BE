package com.wedit.weditapp.global.auth.login.service;

import com.wedit.weditapp.domain.member.domain.Member;
import com.wedit.weditapp.domain.member.domain.repository.MemberRepository;
import com.wedit.weditapp.global.auth.jwt.JwtProvider;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenManager {
    private final JwtProvider jwtProvider;
    private final RefreshTokenService refreshTokenService;
    private final MemberRepository memberRepository;

    public void issueNewTokens(HttpServletResponse response, String email, boolean removeOld) {

        // 1. DB에서 사용자 조회
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("사용자를 찾을 수 없습니다: " + email));

        // 2. 새 Access Token & Refresh Token 생성
        String newAccessToken = jwtProvider.createAccessToken(member.getEmail());
        String newRefreshToken = jwtProvider.createRefreshToken(member.getEmail());

        // 3. 기존 Refresh Token 삭제
        if (removeOld) {
            String existingRefresh = refreshTokenService.getRefreshToken(email);
            if (existingRefresh != null && !existingRefresh.equals(newRefreshToken)) {
                refreshTokenService.deleteRefreshToken(email);
                log.info("기존 RefreshToken 삭제 for email {}: [HIDDEN]", email);
            }
        }

        // 4. Redis에 새 Refresh Token 저장
        refreshTokenService.saveRefreshToken(email, newRefreshToken);
        log.info("새 RefreshToken 저장 for email {}: [HIDDEN]", email);

        // 5. 클라이언트로 Access Token 전송
        jwtProvider.sendAccessAndRefreshToken(response, newAccessToken);
    }
}
