package com.wedit.weditapp.global.auth.login.controller;

import com.wedit.weditapp.domain.member.domain.Member;
import com.wedit.weditapp.domain.member.domain.repository.MemberRepository;
import com.wedit.weditapp.global.auth.jwt.JwtProvider;
import com.wedit.weditapp.global.error.ErrorCode;
import com.wedit.weditapp.global.error.exception.CommonException;
import com.wedit.weditapp.global.response.GlobalResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    @Value("${cookie-domain}")
    private String cookieDomain;

    private final JwtProvider jwtProvider;
    private final MemberRepository memberRepository;

    @Operation(summary = "사용자 정보 갱신", description = "쿠키에 들어있는 Access Token을 이용하여 사용자 정보를 가져옵니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "사용자 정보 조회 성공"),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 토큰"),
            @ApiResponse(responseCode = "404", description = "유저를 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 에러")
    })
    @GetMapping("/renew")
    public ResponseEntity<GlobalResponseDto<Map<String, String>>> getUserInfo(HttpServletRequest request) {
        // 1. 쿠키에서 Access Token 추출
        String accessToken = jwtProvider.extractAccessCookie(request)
                .orElseThrow(() -> new CommonException(ErrorCode.INVALID_TOKEN));

        // 2. 토큰 유효성 검사
        if (!jwtProvider.validateToken(accessToken)) {
            throw new CommonException(ErrorCode.EXPIRED_JWT_TOKEN);
        }

        // 3. 토큰에서 이메일(claim) 추출
        String email = jwtProvider.extractEmail(accessToken)
                .orElseThrow(() -> new CommonException(ErrorCode.INVALID_JWT_SIGNATURE));

        // 4. DB에서 사용자 조회
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CommonException(ErrorCode.USER_NOT_FOUND));

        // 5. 사용자 정보 구성
        Map<String, String> userInfo = Map.of(
                "email", member.getEmail(),
                "name", member.getName()
        );

        return ResponseEntity.ok(GlobalResponseDto.success(userInfo));
    }

    @Operation(summary = "사용자 로그아웃", description = "해당 사용자의 Access Token과 Refresh Token을 삭제하여 로그아웃 시킵니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "사용자 정보 조회 성공"),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 토큰"),
            @ApiResponse(responseCode = "404", description = "유저를 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 에러")
    })
    @PostMapping("/logout")
    public ResponseEntity<GlobalResponseDto<Map<String, String>>> logout(HttpServletRequest request,
                                                                         HttpServletResponse response) {
        // 1. 쿠키에서 Refresh Token 추출
        String refreshToken = jwtProvider.extractRefreshCookie(request)
                .orElse(null);

        // 2. Refresh Token이 있다면 DB에서 무효화
        if (refreshToken != null) {
            jwtProvider.extractEmail(refreshToken).ifPresent(email -> {
                memberRepository.findByEmail(email).ifPresent(member -> {
                    member.updateRefreshToken(null);
                    memberRepository.save(member);
                });
            });
        }

        // 3) 쿠키 만료 처리 (Access/Refresh Token 둘 다)
        expireCookie(response, "accessToken");
        expireCookie(response, "refreshToken");

        // 4) 응답
        return ResponseEntity.ok(
                GlobalResponseDto.success(Map.of("message", "로그아웃 성공!"))
        );
    }

    private void expireCookie(HttpServletResponse response, String cookieName) {
        Cookie cookie = new Cookie(cookieName, null);
        cookie.setDomain(cookieDomain);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setAttribute("SameSite", "None");
        cookie.setMaxAge(0);

        response.addCookie(cookie);
    }
}

