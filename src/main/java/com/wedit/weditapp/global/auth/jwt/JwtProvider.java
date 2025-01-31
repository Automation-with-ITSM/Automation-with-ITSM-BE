package com.wedit.weditapp.global.auth.jwt;

import com.wedit.weditapp.global.error.ErrorCode;
import com.wedit.weditapp.global.error.exception.CommonException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class JwtProvider {

    @Value("${jwt.secretKey}")
    private String secretKey;

    @Value("${jwt.access.expiration}")
    private long accessTokenExpiry;  // 만료 시간 : 3600000 (1시간)

    @Value("${jwt.refresh.expiration}")
    private long refreshTokenExpiry; // 만료 시간 : 1209600000 (2주)

    private Key key; // 실제 사용할 HMAC용 key 객체

    private static final String EMAIL_CLAIM = "email";
    private static final String BEARER = "Bearer ";
    private static final String REFRESH_COOKIE_NAME = "refreshToken"; // 쿠키에 저장할 이름


    // SecretKey 초기화
    @PostConstruct
    protected void init() {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    // Access Token 생성
    public String createAccessToken(String email) {
        return buildToken(email, "AccessToken", accessTokenExpiry);
    }

    // Refresh Token 생성
    public String createRefreshToken() {
        return buildToken(null, "RefreshToken", refreshTokenExpiry);
    }

    // Access Token + Refresh Token 생성 로직
    private String buildToken(String email, String subject, long expiryTime) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expiryTime);

        JwtBuilder builder = Jwts.builder()
                .subject(subject)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256);

        if (email != null) {
            builder.claim(EMAIL_CLAIM, email);
        }

        return builder.compact();
    }

    // AccessToken : JSON Body로 반환
    public void sendAccessTokenResponse(HttpServletResponse response, String accessToken) {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            response.getWriter().write("{\"accessToken\": \"" + accessToken + "\"}");
        } catch (Exception e) {
            log.error("AccessToken JSON 응답 오류: {}", e.getMessage());
        }
    }

    // Refresh Token : HttpOnly, Secure 쿠키로 설정
    public void addRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        Cookie refreshCookie = new Cookie(REFRESH_COOKIE_NAME, refreshToken);
        refreshCookie.setHttpOnly(true); // JavaScript에서 접근 불가능
        refreshCookie.setSecure(true); // HTTPS 환경에서만 전송
        refreshCookie.setPath("/"); // 모든 경로에서 유효
        refreshCookie.setMaxAge((int) TimeUnit.MILLISECONDS.toSeconds(refreshTokenExpiry)); // 만료 시간 설정

        response.addCookie(refreshCookie);
        log.info("Refresh Token이 쿠키에 저장되었습니다.");
    }

    // AccessToken에서 이메일 클레임 추출
    public Optional<String> extractEmail(String accessToken) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith((SecretKey) key)
                    .build()
                    .parseSignedClaims(accessToken)
                    .getPayload();

            return Optional.ofNullable(claims.get(EMAIL_CLAIM, String.class));
        } catch (JwtException e) {
            log.error("유효하지 않은 AccessToken입니다. {}", e.getMessage());
            return Optional.empty();
        }
    }

    // HTTP 요청의 Authorization 헤더에서 Bearer Token 추출
    public Optional<String> extractAccessToken(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader("Authorization"))
                .filter(token -> token.startsWith("Bearer "))
                .map(token -> token.substring(7)); // "Bearer " 제거 후 순수 토큰 반환
    }

    // Token 유효성 검증
    public boolean validateToken(String token) {
        try {
            // 토큰 파싱 -> 에러 없으면 유효
            Jwts.parser()
                    .verifyWith((SecretKey) key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.error("잘못된 JWT 서명 : {}", e.getMessage());
            throw new CommonException(ErrorCode.INVALID_JWT_SIGNATURE);
        } catch (ExpiredJwtException e) {
            log.error("만료된 JWT 토큰 : {}", e.getMessage());
            throw new CommonException(ErrorCode.EXPIRED_JWT_TOKEN);
        } catch (UnsupportedJwtException e) {
            log.error("지원되지 않는 JWT 토큰 : {}", e.getMessage());
            throw new CommonException(ErrorCode.UNSUPPORTED_JWT_TOKEN);
        } catch (IllegalArgumentException e) {
            log.error("JWT 토큰 핸들러 컴팩트 오류 : {}", e.getMessage());
            throw new CommonException(ErrorCode.ILLEGAL_JWT);
        }
    }
}
