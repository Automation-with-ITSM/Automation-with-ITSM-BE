package com.wedit.weditapp.domain.decisions.service;

import com.wedit.weditapp.domain.decisions.domain.repository.DecisionRepository;
import com.wedit.weditapp.domain.decisions.dto.response.CountResponseDto;
import com.wedit.weditapp.domain.shared.DecisionSide;
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

}
