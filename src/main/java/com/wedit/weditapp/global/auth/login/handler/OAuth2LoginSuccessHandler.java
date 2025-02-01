package com.wedit.weditapp.global.auth.login.handler;

import com.wedit.weditapp.domain.member.domain.Member;
import com.wedit.weditapp.domain.member.domain.repository.MemberRepository;
import com.wedit.weditapp.global.auth.login.domain.CustomOAuth2User;
import com.wedit.weditapp.global.auth.jwt.JwtProvider;
import com.wedit.weditapp.global.auth.login.service.RefreshTokenService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {
    private final JwtProvider jwtProvider;
    private final MemberRepository memberRepository;
    private final RefreshTokenService refreshTokenService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        log.info("OAuth2 Login 성공!");

        // 1. principal에서 현재 사용자 정보 획득
        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getEmail();

        // 2. DB에서 회원 조회
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("로그인 성공했으나 DB에 회원 정보가 없음: " + email));

        // 3. Refresh Token 검사 및 재발급
        String oldRefreshToken = refreshTokenService.getRefreshToken(email);
        boolean needNewRefresh = true;

        if (oldRefreshToken != null && jwtProvider.validateToken(oldRefreshToken)) {
            needNewRefresh = false;
            log.info("기존 Refresh Token 유효, 재발급 없이 그대로 사용");
        }

        // 4. 새로 발급할 Refresh Token 결정
        String refreshTokenToUse;
        if (needNewRefresh) {
            refreshTokenToUse = jwtProvider.createRefreshToken(email);
            refreshTokenService.saveRefreshToken(email, refreshTokenToUse);
            log.info("기존 Refresh Token이 없거나 만료되어, 새 Refresh Token 발급 및 Redis 저장");
        } else {
            refreshTokenToUse = oldRefreshToken;
        }

        // 5. Access Token 생성
        String newAccessToken = jwtProvider.createAccessToken(email);

        // 6. Access Token -> 응답 헤더, Refresh Token -> HttpOnly Cookie
        jwtProvider.setAccessTokenHeader(response, newAccessToken);
        jwtProvider.setRefreshTokenCookie(response, refreshTokenToUse);

        // 7. 배포되는 서버용 - 원하는 페이지로 리다이렉트
        //log.info("리다이렉트: http://wedit.site/");
        //response.sendRedirect("http://wedit.site/");

        // 7. 로컬테스트용
        response.setStatus(HttpServletResponse.SC_OK);
    }
}
