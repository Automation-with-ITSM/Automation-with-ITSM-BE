package com.wedit.weditapp.domain.invitation.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.wedit.weditapp.domain.bankAccounts.dto.BankAccountDto;
import com.wedit.weditapp.domain.bankAccounts.service.BankAccountService;
import com.wedit.weditapp.domain.comment.domain.Comment;
import com.wedit.weditapp.domain.comment.domain.repository.CommentRepository;
import com.wedit.weditapp.domain.comment.dto.response.CommentResponseDto;
import com.wedit.weditapp.domain.comment.service.CommentService;
import com.wedit.weditapp.domain.decision.service.DecisionService;
import com.wedit.weditapp.domain.image.dto.response.ImageResponseDto;
import com.wedit.weditapp.domain.image.service.ImageService;
import com.wedit.weditapp.domain.invitation.domain.Invitation;
import com.wedit.weditapp.domain.invitation.domain.repository.InvitationRepository;
import com.wedit.weditapp.domain.invitation.dto.request.InvitationCreateRequestDto;
import com.wedit.weditapp.domain.invitation.dto.request.InvitationUpdateRequestDto;
import com.wedit.weditapp.domain.invitation.dto.response.InvitationResponseDto;
import com.wedit.weditapp.domain.invitation.dto.response.StatisticsDto;
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
	private final CommentService commentService;
	private final DecisionService decisionService;
	private final CommentRepository commentRepository;
	private static final int MAX_INVITATIONS = 10;


	// 청첩장 정보 등록 -> 생성
	public Void createInvitation(UserDetails userDetails, InvitationCreateRequestDto invitationRequest, List<MultipartFile> images) {
		Member member = getMember(userDetails);

		// 초대장 생성 요청이 null인지 확인
		if (invitationRequest == null) {
			throw new CommonException(ErrorCode.INVALID_INPUT_VALUE);
		}

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
			invitationRequest.getTime(),
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

		// 초대장 생성 후, 회원의 청첩장 개수를 확인하고 초과된 청첩장 삭제
		cleanUpExcessInvitations(userDetails);

		return null;
	}

	// 청첩장 조회
	public InvitationResponseDto getInvitation(UserDetails userDetails, Long invitationId) {
		Member member = getMember(userDetails);

		// 초대장 조회
		Invitation invitation  = invitationRepository.findByIdAndMember(invitationId, member)
			.orElseThrow(() -> new CommonException(ErrorCode.INVITATION_NOT_FOUND));

		List<BankAccountDto> bankAccounts = bankAccountService.getBankAccounts(invitation);
		List<ImageResponseDto> images = imageService.getImages(invitation);
		List<Comment> comments = commentRepository.findByInvitation(invitation);

		return InvitationResponseDto.of(invitation, bankAccounts, images,
			comments.stream()
				.map(CommentResponseDto::from)
				.collect(Collectors.toList()));
	}

	// 청첩장 목록 조회 (생성일 기준 오름차순)
	public List<InvitationResponseDto> getMemberInvitations(UserDetails userDetails) {
		Member member = getMember(userDetails);
		List<Invitation> invitations = invitationRepository.findByMemberIdOrderByCreatedAtAsc(member.getId());

		return invitations.stream()
				.map(invitation -> InvitationResponseDto.of(
						invitation,
						bankAccountService.getBankAccounts(invitation),
						imageService.getImages(invitation),
						commentRepository.findByInvitation(invitation).stream()
								.map(CommentResponseDto::from)
								.collect(Collectors.toList())
				))
				.collect(Collectors.toList());
	}

	// 청첩장 수정
	public void updateInvitation(UserDetails userDetails, Long invitationId, InvitationUpdateRequestDto updateRequest, List<MultipartFile> newImages) {
		Member member = getMember(userDetails);

		// 요청 데이터가 비었는지 확인
		if (updateRequest == null) {
			throw new CommonException(ErrorCode.INVALID_INPUT_VALUE);
		}

		Invitation invitation = invitationRepository.findByIdAndMember(invitationId, member)
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
			updateRequest.getTime(),
			updateRequest.getTheme(),
			updateRequest.isGuestBookOption(),
			updateRequest.isDecisionOption(),
			updateRequest.isAccountOption()
		);

		// 계좌 정보 업데이트 / isAccountOption이 false로 변경될 경우 계좌 정보 삭제
		if (updateRequest.isAccountOption() && updateRequest.getBankAccounts() != null) {
			bankAccountService.updateBankAccount(updateRequest.getBankAccounts(), invitation);
		}else if (!updateRequest.isAccountOption()) {
			bankAccountService.deleteBankAccount(invitation);
		}

		// 이미지 정보 업데이트
		if (newImages != null && !newImages.isEmpty()) {
			imageService.updateImages(newImages, invitation);
		}

		// guestBookOption이 false로 변경된 경우 방명록 삭제
		if (!updateRequest.isGuestBookOption()) {
			commentService.deleteComment(invitation);
		}

		// decisionOption이 false로 변경된 경우 참석 의사 삭제
		if (!updateRequest.isDecisionOption()) {
			decisionService.deleteDecision(invitation);
		}

		invitationRepository.save(invitation);
	}

	// url 생성
	public String generateAndSaveInvitationUrl(UserDetails userDetails, Long invitationId) {
		Member member = getMember(userDetails);

		// 청첩장 조회
		Invitation invitation = invitationRepository.findByIdAndMember(invitationId, member)
			.orElseThrow(() -> new CommonException(ErrorCode.INVITATION_NOT_FOUND));

		// URL 생성
		if (invitation.getDistribution() == null) {
			String uniqueCode = invitation.getUniqueId();// 고유 코드 생성
			String generatedUrl = "https://wedit.site/wedding-invitation/" + uniqueCode;

			// URL 저장
			invitation.updateUrl(generatedUrl);
			System.out.println("생성된 url = " + invitation.getDistribution());
			invitationRepository.save(invitation);
		}

		return invitation.getDistribution();
	}

	// 청첩장 삭제
	public void deleteInvitation(UserDetails userDetails, Long invitationId) {
		Member member = getMember(userDetails);
		// 청첩장 조회
		Invitation invitation = invitationRepository.findByIdAndMember(invitationId, member)
			.orElseThrow(() -> new CommonException(ErrorCode.INVITATION_NOT_FOUND));

		// 1. BankAccount 삭제
		bankAccountService.deleteBankAccount(invitation);

		// 2. Image 삭제
		imageService.deleteImages(invitation);

		// 3. Comment 삭제
		commentService.deleteComment(invitation);

		// 4. Decision 삭제
		decisionService.deleteDecision(invitation);

		// 5. Invitation 삭제
		invitationRepository.delete(invitation);
	}

	// 10개 초과된 청첩장 삭제
	public void cleanUpExcessInvitations(UserDetails userDetails) {
		Member member = getMember(userDetails);
		Long memberId = member.getId();

		List<Invitation> invitations = invitationRepository.findByMemberIdOrderByCreatedAtAsc(memberId);
		if (invitations.size() > MAX_INVITATIONS) {
			int excessCount = invitations.size() - MAX_INVITATIONS;
			List<Invitation> excessInvitations = invitations.subList(0, excessCount);
			List<Long> excessInvitationIds = excessInvitations.stream()
					.map(Invitation::getId)
					.collect(Collectors.toList());

			for (Long invitationId : excessInvitationIds) {
				deleteInvitation(userDetails, invitationId);
			}
		}
	}

	// 비회원 청첩장 조회
	public InvitationResponseDto getInvitationForGuest(String uniqueId) {
		// UUID 기반으로 청첩장 조회
		Invitation invitation = invitationRepository.findByUniqueId(uniqueId)
			.orElseThrow(() -> new CommonException(ErrorCode.INVITATION_NOT_FOUND));

		List<BankAccountDto> bankAccounts = bankAccountService.getBankAccounts(invitation);
		List<ImageResponseDto> images = imageService.getImages(invitation);
		List<Comment> comments = commentRepository.findByInvitation(invitation);

		invitation.updateDailyVisitors();
		invitation.updateTotalVisitors();

		return InvitationResponseDto.of(invitation, bankAccounts, images,
			comments.stream()
				.map(CommentResponseDto::from)
				.collect(Collectors.toList()));
	}

	// 멤버 찾기
	private Member getMember(UserDetails userDetails) {
		return memberRepository.findByEmail(userDetails.getUsername())
			.orElseThrow(() -> new CommonException(ErrorCode.USER_NOT_FOUND));
	}

	public StatisticsDto getInvitationStatistics(UserDetails userDetails, Long invitationId) {
		Member member = getMember(userDetails);

		Invitation invitation = invitationRepository.findById(invitationId)
			.orElseThrow(() -> new CommonException(ErrorCode.INVITATION_NOT_FOUND));

		return StatisticsDto.of(invitation, decisionService.getDecisionCounts(invitationId));
	}

}
