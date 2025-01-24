package com.wedit.weditapp.domain.invitation.service;

import java.util.List;

import com.wedit.weditapp.domain.bankAccounts.domain.repository.BankAccountRepository;
import com.wedit.weditapp.domain.comment.domain.repository.CommentRepository;
import com.wedit.weditapp.domain.decision.domain.repository.DecisionRepository;
import com.wedit.weditapp.domain.image.domain.repository.ImageRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.wedit.weditapp.domain.bankAccounts.dto.BankAccountDto;
import com.wedit.weditapp.domain.bankAccounts.service.BankAccountService;
import com.wedit.weditapp.domain.image.dto.response.ImageResponseDto;
import com.wedit.weditapp.domain.image.service.ImageService;
import com.wedit.weditapp.domain.invitation.domain.Invitation;
import com.wedit.weditapp.domain.invitation.domain.repository.InvitationRepository;
import com.wedit.weditapp.domain.invitation.dto.request.InvitationCreateRequestDto;
import com.wedit.weditapp.domain.invitation.dto.response.InvitationResponseDto;
import com.wedit.weditapp.domain.member.domain.Member;
import com.wedit.weditapp.domain.member.domain.repository.MemberRepository;
import com.wedit.weditapp.global.error.ErrorCode;
import com.wedit.weditapp.global.error.exception.CommonException;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class InvitationService {
	private final InvitationRepository invitationRepository;
	private final ImageService imageService;
	private final MemberRepository memberRepository;
	private final BankAccountService bankAccountService;
	private final CommentRepository commentRepository;
	private final DecisionRepository decisionRepository;
	private final ImageRepository imageRepository;
	private final BankAccountRepository bankAccountRepository;

	public Void createInvitation(Long memberId, InvitationCreateRequestDto invitationRequest, List<MultipartFile> images) {
		Member member = getMember(memberId);
		// Invitation 생성
		Invitation invitation = Invitation.createInvitation(
			member,
			invitationRequest.getGroom(),
			invitationRequest.getBride(),
			invitationRequest.getGroomF(),
			invitationRequest.getGroomM(),
			invitationRequest.getBrideF(),
			invitationRequest.getBrideM(),
			invitationRequest.getAddress(),
			invitationRequest.getExtraAddress(),
			invitationRequest.getDate(),
			invitationRequest.getTheme(),
			invitationRequest.getDistribution(),
			invitationRequest.isGuestBookOption(),
			invitationRequest.isDecisionOption(),
			invitationRequest.isAccountOption()
		);

		// 초대장 저장
		invitationRepository.save(invitation);

		// 계좌 정보 저장
		if (invitationRequest.isAccountOption() && invitationRequest.getBankAccounts() != null) {
			bankAccountService.createBankAccounts(invitationRequest.getBankAccounts(), invitation);
		}

		// 이미지 저장
		imageService.saveImages(images, invitation);

		return null;
		//return InvitationResponseDTO.from(invitationRepository.save(invitation));
	}

	public InvitationResponseDto getInvitation(Long invitationId) {
		// 초대장 조회
		Invitation invitation  = invitationRepository.findById(invitationId)
			.orElseThrow(() -> new CommonException(ErrorCode.INVITATION_NOT_FOUND));

		List<BankAccountDto> bankAccounts = bankAccountService.getBankAccounts(invitation);
		List<ImageResponseDto> images = imageService.getImages(invitation);

		return InvitationResponseDto.from(invitation, bankAccounts, images);
	}

	private Member getMember(Long memberId) {
		return memberRepository.findById(memberId)
			.orElseThrow(() -> new CommonException(ErrorCode.USER_NOT_FOUND));
	}

	// Invitation 삭제시 연관된 Comment, Decision, Image, BankAccount 삭제
	private void deleteInvitation(Long invitationId){
		// Invitation 유효성 검사
		Invitation invitation = invitationRepository.findById(invitationId)
				.orElseThrow(() -> new CommonException(ErrorCode.INVITATION_NOT_FOUND));

		commentRepository.deleteAllByInvitationId(invitationId);
		decisionRepository.deleteAllByInvitationId(invitationId);
		imageRepository.deleteAllByInvitationId(invitationId);
		bankAccountRepository.deleteAllByInvitationId(invitationId);

		invitationRepository.delete(invitation);
	}
}
