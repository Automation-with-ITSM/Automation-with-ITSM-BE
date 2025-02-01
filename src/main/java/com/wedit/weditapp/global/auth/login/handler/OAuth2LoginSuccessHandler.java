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

        // 3. AccessToken & RefreshToken 발급 + DB 저장
        String accessToken = jwtProvider.createAccessToken(member.getEmail());
        String refreshToken = member.getRefreshToken(); // 추후 Redis 추가할 때 변경

        if (refreshToken == null || !jwtProvider.validateToken(refreshToken)) {
            //refreshToken = jwtProvider.createRefreshToken();
            member.updateRefreshToken(refreshToken);
            memberRepository.save(member);
            log.info("새 Refresh Token 발급 및 저장 완료"); // 이거 보여주면 안되서 지움
        }

        // 5. Access Token은 JSON Body로 반환, Refresh Token은 HttpOnly Secure Cookie에 저장
        //jwtProvider.sendAccessTokenResponse(response, accessToken);
        jwtProvider.setRefreshTokenCookie(response, refreshToken);

        // 로그인 성공 후 프런트 : `/redirect`로 이동 & 백엔드 : Access Token 보여주기
        if (isFrontendAvailable("http://localhost:5173/redirect")) {
            log.info("Redirecting to frontend: http://localhost:5173/redirect");
            response.sendRedirect("http://localhost:5173/redirect");
        } else {
            log.warn("프론트엔드가 실행되지 않음. Access Token을 JSON 응답으로 반환합니다.");
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"accessToken\": \"" + accessToken + "\"}");
        }
    }

    private boolean isFrontendAvailable(String url) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(1000);
            connection.setReadTimeout(1000);
            int responseCode = connection.getResponseCode();
            return (200 <= responseCode && responseCode <= 399);
        } catch (IOException e) {
            log.warn("프론트엔드 서버({})가 실행되지 않음.", url);
            return false;
        }
    }
}
