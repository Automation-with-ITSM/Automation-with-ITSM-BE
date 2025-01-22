package com.wedit.weditapp.domain.invitation.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.wedit.weditapp.domain.bankAccounts.domain.BankAccounts;
import com.wedit.weditapp.domain.bankAccounts.dto.BankAccountDTO;
import com.wedit.weditapp.domain.bankAccounts.service.BankAccountService;
import com.wedit.weditapp.domain.image.service.ImageService;
import com.wedit.weditapp.domain.invitation.domain.Invitation;
import com.wedit.weditapp.domain.invitation.domain.repository.InvitationRepository;
import com.wedit.weditapp.domain.invitation.dto.request.InvitationCreateRequestDTO;
import com.wedit.weditapp.domain.invitation.dto.response.InvitationResponseDTO;
import com.wedit.weditapp.domain.member.domain.Member;
import com.wedit.weditapp.domain.member.domain.repository.MemberRepository;
import com.wedit.weditapp.domain.shared.Theme;
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

	public Void createInvitation(Long memberId, InvitationCreateRequestDTO invitationRequest, List<MultipartFile> images) {
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

	public InvitationResponseDTO getInvitation(Long invitationId) {
		// 초대장 조회
		Invitation invitation  = invitationRepository.findById(invitationId)
			.orElseThrow(() -> new CommonException(ErrorCode.INVITATION_NOT_FOUND));

		List<BankAccountDTO> bankAccounts = bankAccountService.getBankAccounts(invitation);

		return InvitationResponseDTO.from(invitation, bankAccounts);
	}

	private Member getMember(Long memberId) {
		return memberRepository.findById(memberId)
			.orElseThrow(() -> new CommonException(ErrorCode.USER_NOT_FOUND));
	}
}
