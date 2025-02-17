package com.wedit.weditapp.domain.member.controller;

import com.wedit.weditapp.domain.invitation.dto.response.InvitationResponseDto;
import com.wedit.weditapp.domain.invitation.dto.response.StatisticsDto;
import com.wedit.weditapp.domain.invitation.service.InvitationService;
import com.wedit.weditapp.global.response.GlobalResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final InvitationService invitationService;

    // 마이페이지 - 해당 회원의 청첩장 목록 불러오기 API
    @Operation(summary = "청첩장 목록 조회", description = "해당 회원이 제작한 모든 청첩장들의 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "유저를 찾을 수 없습니다."),
            @ApiResponse(responseCode = "500", description = "서버 에러")
    })
    @GetMapping("/mypage")
    public ResponseEntity<GlobalResponseDto<List<InvitationResponseDto>>> getMemberInvitations(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<InvitationResponseDto> invitations = invitationService.getMemberInvitations(userDetails);

        return ResponseEntity.status(HttpStatus.OK).body(GlobalResponseDto.success(invitations));
    }

    @GetMapping("/{invitationId}/analysis")
    @Operation(summary = "분석 보기 조회", description = "분석 보기를 조회합니다.")
    public ResponseEntity<GlobalResponseDto<StatisticsDto>> getInvitationStatistics(
        @AuthenticationPrincipal UserDetails userDetails,
        @PathVariable Long invitationId) {

        return ResponseEntity.status(HttpStatus.OK)
            .body(GlobalResponseDto.success(invitationService.getInvitationStatistics(userDetails, invitationId)));
    }
}
