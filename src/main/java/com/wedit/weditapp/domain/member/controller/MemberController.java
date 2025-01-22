package com.wedit.weditapp.domain.member.controller;

import com.wedit.weditapp.domain.member.dto.response.MemberResponseDto;
import com.wedit.weditapp.domain.member.service.MemberService;
import com.wedit.weditapp.global.response.GlobalResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    // 모든 회원 조회 API
    @Operation(summary = "모든 회원 조회", description = "등록된 모든 회원의 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "회원 목록을 찾을 수 없습니다."),
            @ApiResponse(responseCode = "500", description = "서버 에러")
    })
    @GetMapping
    public ResponseEntity<GlobalResponseDto<List<MemberResponseDto>>> findAllMembers() {
        List<MemberResponseDto> memberResponses = memberService.findAllMembers();
        return ResponseEntity.ok(GlobalResponseDto.success(memberResponses));
    }

    // 단일 회원 조회 API
    @Operation(summary = "단일 회원 조회", description = "해당 id를 가진 회원의 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "해당 회원을 찾을 수 없습니다."),
            @ApiResponse(responseCode = "500", description = "서버 에러")
    })
    @GetMapping("/{userId}")
    public ResponseEntity<GlobalResponseDto<MemberResponseDto>> findMember(
            @PathVariable Long userId) {
        MemberResponseDto memberResponse = memberService.findMember(userId);
        return ResponseEntity.ok(GlobalResponseDto.success(memberResponse));
    }
}
