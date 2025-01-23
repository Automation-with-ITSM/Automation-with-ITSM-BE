package com.wedit.weditapp.domain.decision.service;

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
            throw new CommonException(ErrorCode.INVALID_INPUT_VALUE);
        }

        Integer totalCount = decisionRepository.getTotalDecisionCount(invitationId);
        Integer groomCount = decisionRepository.getGroomDecisionCount(invitationId);
        Integer brideCount = decisionRepository.getBrideDecisionCount(invitationId);

        return CountResponseDto.of(totalCount, groomCount, brideCount);
    }

    // 특정 청첩장에 참석의사 등록
    public Decision createDecision(DecisionCreateRequestDto decisionCreateRequestDTO) {

        Invitation invitation = invitationRepository.findById(decisionCreateRequestDTO.getInvitationId())
                .orElseThrow(() -> new CommonException(ErrorCode.INVITATION_NOT_FOUND));


        Decision decision = Decision.createDecision(
                decisionCreateRequestDTO.getName(),
                decisionCreateRequestDTO.getPhoneNumber(),
                decisionCreateRequestDTO.getAddPerson(),
                decisionCreateRequestDTO.getSide(),
                invitation
        );

        return decisionRepository.save(decision);
    }

}
