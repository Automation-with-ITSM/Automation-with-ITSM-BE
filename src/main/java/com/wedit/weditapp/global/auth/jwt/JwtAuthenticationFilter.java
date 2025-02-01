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

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Refresh Token이 존재하면 Access Token 재발급 시도
        extractRefreshTokenFromCookie(request)
                .filter(jwtProvider::validateToken)
                .ifPresent(refreshToken -> reIssueAccessToken(response, refreshToken));

        // Access Token이 존재하면 인증 수행
        jwtProvider.extractAccessToken(request)
                .filter(jwtProvider::validateToken)
                .ifPresent(this::authenticate);

        filterChain.doFilter(request, response);
    }

    // RefreshToken을 사용하여 AccessToken 재발급
    private void reIssueAccessToken(HttpServletResponse response, String refreshToken) {
        memberRepository.findByRefreshToken(refreshToken)
                .ifPresentOrElse(
                        member -> {
                            String newAccessToken = jwtProvider.createAccessToken(member.getEmail());
                            String newRefreshToken = jwtProvider.createRefreshToken();

                            // DB에 새로운 Refresh Token 저장
                            member.updateRefreshToken(newRefreshToken);
                            memberRepository.save(member);

                            // Access Token은 JSON Body로 반환, Refresh Token은 쿠키로 저장
                            jwtProvider.setAccessTokenHeader(response, newAccessToken);
                            jwtProvider.setRefreshTokenCookie(response, newRefreshToken);
                            log.info("AccessToken 및 RefreshToken 재발급 완료");
                        },
                        () -> log.error("유효하지 않은 RefreshToken으로 재발급 시도")
                );
    }

    // HttpOnly Secure 쿠키에서 Refresh Token 추출
    private Optional<String> extractRefreshTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return Optional.empty();
        }
        return Arrays.stream(request.getCookies())
                .filter(cookie -> "refreshToken".equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst();
    }

    // AccessToken을 사용하여 사용자 인증
    private void authenticate(String accessToken) {
        jwtProvider.extractEmail(accessToken).ifPresent(email -> {
            memberRepository.findByEmail(email).ifPresentOrElse(
                    member -> {
                        /*Authentication authentication = new UsernamePasswordAuthenticationToken(
                                member, null, member.getAuthorities()
                        );
                        SecurityContextHolder.getContext().setAuthentication(authentication);*/
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
