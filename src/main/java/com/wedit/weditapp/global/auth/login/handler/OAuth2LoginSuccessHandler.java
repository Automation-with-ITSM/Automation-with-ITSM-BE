package com.wedit.weditapp.global.auth.login.handler;

import com.wedit.weditapp.domain.member.domain.Member;
import com.wedit.weditapp.domain.member.domain.repository.MemberRepository;
import com.wedit.weditapp.global.auth.login.domain.CustomOAuth2User;
import com.wedit.weditapp.global.auth.jwt.JwtProvider;
import com.wedit.weditapp.global.auth.login.service.RefreshTokenService;
import com.wedit.weditapp.global.auth.login.service.TokenManager;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {
    private final JwtProvider jwtProvider;
    private final MemberRepository memberRepository;
    private final RefreshTokenService refreshTokenService;
    private final TokenManager tokenManager;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        log.info("OAuth2 Login 성공!");

        // 1. principal에서 현재 사용자 정보 획득
        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getEmail();

        // 2. DB에서 회원 조회
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("로그인 성공했으나 DB에 회원 정보가 없음: " + email));

        // 4. 새 토큰 발급 & Redis 저장 & 응답 전송
        tokenManager.issueNewTokens(response, email, true);


        // 5. 응답
        response.setStatus(HttpServletResponse.SC_OK);
    }
}
