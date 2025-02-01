package com.wedit.weditapp.global.auth.jwt;

import com.wedit.weditapp.domain.member.domain.Member;
import com.wedit.weditapp.domain.member.domain.repository.MemberRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.NullAuthoritiesMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final MemberRepository memberRepository;
    private final GrantedAuthoritiesMapper authoritiesMapper = new NullAuthoritiesMapper();

    private static final String SUPER_TOKEN = "super-secret-token"; // 슈퍼 토큰

    @Override
    protected void doFilterInternal(HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {

        // 슈퍼 토큰 예외 처리 (테스트용)
        String token = request.getHeader("Authorization");
        if (token != null && token.equals("Bearer " + SUPER_TOKEN)) {
            log.warn("Super Token 사용 - 모든 요청 인증 우회");
            setSuperUserAuthentication();
            filterChain.doFilter(request, response);
            return;
        }

        // 쿠키에서 Refresh Token 추출 & 유효성 검사 -> 재발급 로직
        extractCookie(request, "refreshToken")
            .filter(jwtProvider::validateToken)
            .ifPresent(refreshToken -> reIssueTokens(response, refreshToken));

        // 쿠키에서 Access Token 추출 & 유효성 검사 -> 인증 처리
        extractCookie(request, "accessToken")
            .filter(jwtProvider::validateToken)
            .ifPresent(this::authenticate);

        filterChain.doFilter(request, response);
    }

    private void setSuperUserAuthentication() {
        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
            .username("superuser")
            .password("")
            .roles("ADMIN") // 슈퍼 계정 권한 설정 가능
            .build();

        Authentication authentication = new UsernamePasswordAuthenticationToken(
            userDetails, null, userDetails.getAuthorities()
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    // 1. RefreshToken 유효 및 존재 -> 새로운 Access Token & Refresh Token 재발급
    // 2. DB에 새 Refresh Token 저장 (추후 Redis 대체)
    // 3. 응답에 Access Token(쿠키) & Refresh Token(쿠키) 설정
    private void reIssueTokens(HttpServletResponse response, String refreshToken) {
        memberRepository.findByRefreshToken(refreshToken)
            .ifPresentOrElse(
                member -> {
                    // 새 Access Token / 새 Refresh Token 생성
                    String newAccessToken = jwtProvider.createAccessToken(member.getEmail());
                    String newRefreshToken = jwtProvider.createRefreshToken(member.getEmail());

                    // DB에 새로운 Refresh Token 저장
                    member.updateRefreshToken(newRefreshToken);
                    memberRepository.save(member);

                    // Access Token은 헤더로, Refresh Token은 쿠키로 저장
                    jwtProvider.setAccessTokenCookie(response, newAccessToken);
                    jwtProvider.setRefreshTokenCookie(response, newRefreshToken);
                    log.info("AccessToken 및 RefreshToken 재발급 완료");
                },
                () -> log.error("유효하지 않은 RefreshToken으로 재발급 시도")
            );
    }

    // HttpOnly Secure 쿠키에서 Token 추출
    private Optional<String> extractCookie(HttpServletRequest request, String cookieName) {
        if (request.getCookies() == null) {
            return Optional.empty();
        }
        return Arrays.stream(request.getCookies())
            .filter(cookie -> cookieName.equals(cookie.getName()))
            .map(Cookie::getValue)
            .findFirst();
    }

    // AccessToken을 사용하여 사용자 인증
    private void authenticate(String accessToken) {
        jwtProvider.extractEmail(accessToken).ifPresent(email -> {
            memberRepository.findByEmail(email).ifPresentOrElse(
                member -> {
                    setAuthentication(member);
                    log.info("사용자 인증 완료: {}", email);
                },
                () -> log.error("AccessToken의 이메일 정보와 일치하는 사용자가 없습니다.")
            );
        });
    }

    //UserDetails 설정
    private void setAuthentication(Member member) {
        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
            .username(member.getEmail())
            .password("") // 비밀번호는 사용하지 않으므로 빈 문자열
            .roles(member.getRole().name())
            .build();

        Authentication authentication = new UsernamePasswordAuthenticationToken(
            userDetails, null, authoritiesMapper.mapAuthorities(userDetails.getAuthorities())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}