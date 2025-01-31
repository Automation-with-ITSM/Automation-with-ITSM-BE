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
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtProvider jwtProvider;
    private final MemberRepository memberRepository;

    @Operation(summary = "사용자 정보 조회", description = "Access Token을 이용하여 사용자 정보를 가져옵니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "사용자 정보 조회 성공"),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 토큰"),
            @ApiResponse(responseCode = "404", description = "유저를 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 에러")
    })
    @GetMapping("/me")
    public ResponseEntity<GlobalResponseDto<Map<String, String>>> getUserInfo(
            @RequestHeader(value = "Authorization", required = false) String token) {

        if (token == null || !token.startsWith("Bearer ")) {
            throw new CommonException(ErrorCode.INVALID_TOKEN);
        }

        token = token.substring(7);

        if (!jwtProvider.validateToken(token)) {
            throw new CommonException(ErrorCode.EXPIRED_JWT_TOKEN);
        }

        String email = jwtProvider.extractEmail(token)
                .orElseThrow(() -> new CommonException(ErrorCode.INVALID_JWT_SIGNATURE));

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CommonException(ErrorCode.USER_NOT_FOUND));

        Map<String, String> userInfo = Map.of(
                "email", member.getEmail(),
                "name", member.getName()
        );

        return ResponseEntity.ok(GlobalResponseDto.success(userInfo));
    }
}

