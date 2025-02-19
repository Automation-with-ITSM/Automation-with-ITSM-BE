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
import java.util.Arrays;
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

    /*@Value("${cookie-domain}")
    private String cookieDomain;*/

    private Key key; // 실제 사용할 HMAC용 key 객체

    private static final String EMAIL_CLAIM = "email";
    private static final String ACCESS_COOKIE_NAME = "accessToken";
    private static final String REFRESH_COOKIE_NAME = "refreshToken";


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
    public String createRefreshToken(String email) {
        return buildToken(email, "RefreshToken", refreshTokenExpiry);
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

    // Access Token : HttpOnly, Secure 쿠키로 설정
    public void setAccessTokenCookie(HttpServletResponse response, String accessToken) {
        Cookie accessCookie = new Cookie(ACCESS_COOKIE_NAME, accessToken);

        accessCookie.setHttpOnly(true);  // JavaScript에서 접근 불가능
        accessCookie.setSecure(true);    // HTTPS 상황에서만 전송
        accessCookie.setPath("/");
        accessCookie.setAttribute("SameSite", "None");
        accessCookie.setDomain(".wedit.site");
        accessCookie.setMaxAge((int) TimeUnit.MILLISECONDS.toSeconds(accessTokenExpiry));

        response.addCookie(accessCookie);
        log.info("AccessToken 쿠키 저장 완료");
    }

    // Refresh Token : HttpOnly, Secure 쿠키로 설정
    public void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        Cookie refreshCookie = new Cookie(REFRESH_COOKIE_NAME, refreshToken);

        refreshCookie.setHttpOnly(true); // JavaScript에서 접근 불가능
        refreshCookie.setSecure(true); // HTTPS 환경에서만 전송
        refreshCookie.setPath("/");
        refreshCookie.setAttribute("SameSite", "None");
        refreshCookie.setDomain(".wedit.site");
        refreshCookie.setMaxAge((int) TimeUnit.MILLISECONDS.toSeconds(refreshTokenExpiry));

        response.addCookie(refreshCookie);
        log.info("Refresh Token 쿠키 저장 완료");
    }

    // HttpOnly Secure 쿠키에서 Access Token 추출
    public Optional<String> extractAccessCookie(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return Optional.empty();
        }

        return Arrays.stream(request.getCookies())
            .filter(cookie -> ACCESS_COOKIE_NAME.equals(cookie.getName()))
            .map(Cookie::getValue)
            .findFirst();
    }

    // HttpOnly Secure 쿠키에서 Refresh Token 추출
    public Optional<String> extractRefreshCookie(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return Optional.empty();
        }

        return Arrays.stream(request.getCookies())
            .filter(cookie -> REFRESH_COOKIE_NAME.equals(cookie.getName()))
            .map(Cookie::getValue)
            .findFirst();
    }

    // 토큰으로부터 email 클레임 추출
    public Optional<String> extractEmail(String token) {
        try {
            Claims claims = Jwts.parser()
                .verifyWith((SecretKey) key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

            return Optional.ofNullable(claims.get(EMAIL_CLAIM, String.class));
        } catch (JwtException e) {
            log.error("토큰에서 email 클레임 추출 실패: {}", e.getMessage());

            return Optional.empty();
        }
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