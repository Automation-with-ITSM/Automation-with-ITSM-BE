package com.wedit.weditapp.domain.invitation.service;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.wedit.weditapp.domain.bankAccounts.dto.BankAccountDto;
import com.wedit.weditapp.domain.bankAccounts.service.BankAccountService;
import com.wedit.weditapp.domain.comment.domain.Comment;
import com.wedit.weditapp.domain.comment.domain.repository.CommentRepository;
import com.wedit.weditapp.domain.decision.domain.Decision;
import com.wedit.weditapp.domain.decision.domain.repository.DecisionRepository;
import com.wedit.weditapp.domain.image.dto.response.ImageResponseDto;
import com.wedit.weditapp.domain.image.service.ImageService;
import com.wedit.weditapp.domain.invitation.domain.Invitation;
import com.wedit.weditapp.domain.invitation.domain.repository.InvitationRepository;
import com.wedit.weditapp.domain.invitation.dto.request.InvitationCreateRequestDto;
import com.wedit.weditapp.domain.invitation.dto.request.InvitationUpdateRequestDto;
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

	// 청첩장 정보 등록 -> 생성
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

	// 청첩장 조회
	public InvitationResponseDto getInvitation(Long invitationId) {
		// 초대장 조회
		Invitation invitation  = invitationRepository.findById(invitationId)
			.orElseThrow(() -> new CommonException(ErrorCode.INVITATION_NOT_FOUND));

		List<BankAccountDto> bankAccounts = bankAccountService.getBankAccounts(invitation);
		List<ImageResponseDto> images = imageService.getImages(invitation);

		return InvitationResponseDto.from(invitation, bankAccounts, images);
	}

	// 청첩장 수정
	public void updateInvitation(Long invitationId, InvitationUpdateRequestDto updateRequest, List<MultipartFile> newImages) {
		Invitation invitation = invitationRepository.findById(invitationId)
			.orElseThrow(() -> new CommonException(ErrorCode.INVITATION_NOT_FOUND));

		// 청첩장 정보 업데이트
		invitation.updateInvitation(
			updateRequest.getGroom(),
			updateRequest.getBride(),
			updateRequest.getGroomF(),
			updateRequest.getGroomM(),
			updateRequest.getBrideF(),
			updateRequest.getBrideM(),
			updateRequest.getAddress(),
			updateRequest.getExtraAddress(),
			updateRequest.getDate(),
			updateRequest.getTheme(),
			updateRequest.isGuestBookOption(),
			updateRequest.isDecisionOption(),
			updateRequest.isAccountOption()
		);

		// 계좌 정보 업데이트
		if (updateRequest.isAccountOption() && updateRequest.getBankAccounts() != null) {
			bankAccountService.updateBankAccount(updateRequest.getBankAccounts(), invitation);
		}

		// 이미지 정보 업데이트
		if (newImages != null && !newImages.isEmpty()) {
			imageService.updateImages(newImages, invitation);
		}

		//invitationRepository.save(invitation);
	}

	// 멤버 찾기
	private Member getMember(Long memberId) {
		return memberRepository.findById(memberId)
			.orElseThrow(() -> new CommonException(ErrorCode.USER_NOT_FOUND));
	}

	public String generateAndSaveInvitationUrl(Long invitationId) {
		// 청첩장 조회
		Invitation invitation = invitationRepository.findById(invitationId)
			.orElseThrow(() -> new CommonException(ErrorCode.INVITATION_NOT_FOUND));

		// URL 생성
		if (invitation.getDistribution() == null) {
			String uniqueCode = UUID.randomUUID().toString(); // 고유 코드 생성
			String generatedUrl = "https://yourdomain.com/invitations/" + uniqueCode;

			// URL 저장
			invitation.updateUrl(generatedUrl);
			System.out.println("생성된 url = " + invitation.getDistribution());
			invitationRepository.save(invitation);
		}

		return invitation.getDistribution();
	}

	public void deleteInvitation(Long invitationId) {
		// 청첩장 조회
		Invitation invitation = invitationRepository.findById(invitationId)
			.orElseThrow(() -> new CommonException(ErrorCode.INVITATION_NOT_FOUND));

		// 1. BankAccount 삭제
		bankAccountService.deleteBankAccount(invitation);

		// 2. Image 삭제
		imageService.deleteImages(invitation);

		// 3. Comment 삭제
		List<Comment> comments = commentRepository.findByInvitationId(invitationId, Pageable.unpaged()).getContent();
		if (!comments.isEmpty()) {
			commentRepository.deleteAll(comments);
		}

		// 4. Decision 삭제
		List<Decision> decisions = decisionRepository.findByInvitationId(invitationId);
		if (!decisions.isEmpty()) {
			decisionRepository.deleteAll(decisions);
		}

		// 5. Invitation 삭제
		invitationRepository.delete(invitation);
	}

	// 비회원 청첩장 조회
	public InvitationResponseDto getInvitationForGuest(String uniqueId) {
		// UUID 기반으로 청첩장 조회
		Invitation invitation = invitationRepository.findByUniqueId(uniqueId)
			.orElseThrow(() -> new CommonException(ErrorCode.INVITATION_NOT_FOUND));

		List<BankAccountDto> bankAccounts = bankAccountService.getBankAccounts(invitation);
		List<ImageResponseDto> images = imageService.getImages(invitation);

		return InvitationResponseDto.from(invitation, bankAccounts, images);
	}
}
