package com.wedit.weditapp.global.auth.jwt;

import com.wedit.weditapp.domain.member.domain.Member;
import com.wedit.weditapp.domain.member.domain.repository.MemberRepository;
import com.wedit.weditapp.global.auth.login.service.RefreshTokenService;
import com.wedit.weditapp.global.auth.login.service.TokenManager;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
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
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final MemberRepository memberRepository;
    private final RefreshTokenService refreshTokenService;
    private final TokenManager tokenManager;

    private final GrantedAuthoritiesMapper authoritiesMapper = new NullAuthoritiesMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // RefreshToken 처리
        String refreshToken = jwtProvider.extractRefreshToken(request)
                .filter(jwtProvider::validateToken)
                .orElse(null);

        if (refreshToken != null) {
            reIssueAccessToken(response, refreshToken);
            return; // AccessToken 재발급 후 인증 진행 중단
        }

        // AccessToken 처리
        jwtProvider.extractAccessToken(request)
                .filter(jwtProvider::validateToken).ifPresent(this::authenticate);

        filterChain.doFilter(request, response);
    }

    // RefreshToken을 사용하여 AccessToken 재발급 (Redis 추가)
    private void reIssueAccessToken(HttpServletResponse response, String refreshToken) throws IOException {
        // 1. RefreshToken에서 이메일 추출
        Optional<String> emailOpt = jwtProvider.extractEmailFromRefreshToken(refreshToken);
        if (emailOpt.isEmpty()) {
            log.error("RefreshToken에서 이메일 추출 실패: {}", refreshToken);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid Refresh Token");
            return;
        }
        String email = emailOpt.get();

        // 2. Redis에서 저장된 RefreshToken 조회
        String storedRefreshToken = refreshTokenService.getRefreshToken(email);
        if (storedRefreshToken == null || !storedRefreshToken.equals(refreshToken)) {
            log.error("Redis에 저장된 RefreshToken과 일치하지 않음. email = {}", email);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid Refresh Token");
            return;
        }

        // 3. DB에서 사용자 조회
        Optional<Member> optionalMember = memberRepository.findByEmail(email);
        if (optionalMember.isEmpty()) {
            log.error("RefreshToken의 이메일로 회원을 찾을 수 없음. email = {}", email);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid Refresh Token");
            return;
        }

        Member member = optionalMember.get();

        // 4. TokenManager 호출 (새로운 Access/Refresh Token 발급 & Redis 저장 & 응답 전송)
        tokenManager.issueNewTokens(response, email, true);
        log.info("AccessToken 및 RefreshToken 재발급 완료 for email: {}", email);
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
