package com.wedit.weditapp.global.auth.login.handler;

import com.wedit.weditapp.domain.member.domain.Member;
import com.wedit.weditapp.domain.member.domain.repository.MemberRepository;
import com.wedit.weditapp.global.auth.login.domain.CustomOAuth2User;
import com.wedit.weditapp.global.auth.jwt.JwtProvider;
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
        String oldRefreshToken = member.getRefreshToken();
        boolean needNewRefresh = (oldRefreshToken == null || !jwtProvider.validateToken(oldRefreshToken));

        if (needNewRefresh) {
            String newRefreshToken = jwtProvider.createRefreshToken(member.getEmail());
            member.updateRefreshToken(newRefreshToken);
            memberRepository.save(member);

            oldRefreshToken = newRefreshToken; // 갱신
            log.info("Refresh Token이 없거나 만료되어, 새로 발급했습니다.");
        }

        // 4. Access Token 생성
        String newAccessToken = jwtProvider.createAccessToken(member.getEmail());

        // 5. Access Token -> 응답 헤더, Refresh Token -> HttpOnly Cookie
        jwtProvider.setAccessTokenCookie(response, newAccessToken);
        jwtProvider.setRefreshTokenCookie(response, oldRefreshToken);

        // 6. 배포되는 서버용 - 원하는 페이지로 리다이렉트
        //log.info("리다이렉트: http://wedit.site/");
        //response.sendRedirect("http://localhost:5173/");

        // 6. 로컬테스트용
        response.setStatus(HttpServletResponse.SC_OK);
    }
}
