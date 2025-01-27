package com.wedit.weditapp.domain.decision.service;

import java.util.List;

import com.wedit.weditapp.domain.decision.domain.Decision;
import com.wedit.weditapp.domain.decision.domain.repository.DecisionRepository;
import com.wedit.weditapp.domain.decision.dto.request.DecisionCreateRequestDto;
import com.wedit.weditapp.domain.decision.dto.response.CountResponseDto;
import com.wedit.weditapp.domain.invitation.domain.Invitation;
import com.wedit.weditapp.domain.invitation.domain.repository.InvitationRepository;
import com.wedit.weditapp.global.error.ErrorCode;
import com.wedit.weditapp.global.error.exception.CommonException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class DecisionService {

    private final DecisionRepository decisionRepository;

    private final InvitationRepository invitationRepository;

    // 특정 청첩장의 참석의사 누계 조회
    public CountResponseDto getDecisionCounts(Long invitationId){
        // invitationId 유효성 검사
        if (invitationId == null || invitationId <= 0){
            throw new CommonException(ErrorCode.INVALID_INVITATION_ID);
        }

        Integer totalCount = decisionRepository.getTotalDecisionCount(invitationId);
        Integer groomCount = decisionRepository.getGroomDecisionCount(invitationId);
        Integer brideCount = decisionRepository.getBrideDecisionCount(invitationId);

        return CountResponseDto.of(totalCount, groomCount, brideCount);
    }

    // 특정 청첩장에 참석의사 등록
    public Decision createDecision(DecisionCreateRequestDto decisionCreateRequestDto) {

        // 잘못된 청첩장 id일 경우
        if (decisionCreateRequestDto.getInvitationId() == null || decisionCreateRequestDto.getInvitationId() <= 0) {
            throw new CommonException(ErrorCode.INVALID_INVITATION_ID);
        }

        Invitation invitation = invitationRepository.findById(decisionCreateRequestDto.getInvitationId())
                .orElseThrow(() -> new CommonException(ErrorCode.INVITATION_NOT_FOUND));

        Decision decision = Decision.createDecision(
                decisionCreateRequestDto.getName(),
                decisionCreateRequestDto.getPhoneNumber(),
                decisionCreateRequestDto.getAddPerson(),
                decisionCreateRequestDto.getSide(),
                invitation
        );

        return decisionRepository.save(decision);
    }

    // 특정 청첩장의 참성 의사 모두 삭제
    public void deleteDecision(Invitation invitation) {
        List<Decision> decisions = decisionRepository.findByInvitation(invitation);

        if (decisions == null || decisions.isEmpty()) {
            return; // 참석 의사가 없거나 null이면 아무 작업도 하지 않음
        }

        decisionRepository.deleteAll(decisions);
    }
}
