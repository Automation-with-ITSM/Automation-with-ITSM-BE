package com.wedit.weditapp.domain.invitation.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.wedit.weditapp.domain.invitation.dto.request.InvitationCreateRequestDto;

import com.wedit.weditapp.domain.invitation.dto.response.InvitationResponseDto;
import com.wedit.weditapp.domain.invitation.service.InvitationService;
import com.wedit.weditapp.global.response.GlobalResponseDto;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/invitations")
@RequiredArgsConstructor
public class InvitationController {
	private final InvitationService invitationService;

	@PostMapping(path="/{memberId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@Operation(summary = "필수 정보 등록", description = "청첩장 등록 및 이미지 업로드")
	public ResponseEntity<GlobalResponseDto<Void>> createInvitation(
		@RequestPart("images") List<MultipartFile> images,
		@Valid @RequestPart("content") InvitationCreateRequestDto request,
		@PathVariable Long memberId) {
		//@AuthenticationPrincipal UserDetails userDetail

		return ResponseEntity.status(HttpStatus.CREATED)
			.body(GlobalResponseDto.success(invitationService.createInvitation(memberId, request, images)));
	}

	@GetMapping("/{invitationId}")
	@Operation(summary = "청첩장 조회", description = "특정 청첩장 상세 정보 조회")
	public ResponseEntity<GlobalResponseDto<InvitationResponseDto>> getInvitation(
		@PathVariable Long invitationId){
		return ResponseEntity.status(HttpStatus.OK)
			.body(GlobalResponseDto.success(invitationService.getInvitation(invitationId)));
	}
}
