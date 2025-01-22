package com.wedit.weditapp.domain.decisions.service;

import com.wedit.weditapp.domain.decisions.domain.repository.DecisionRepository;
import com.wedit.weditapp.domain.decisions.dto.response.CountDTO;
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

    public CountDTO getDecisionCount(Long invitationId, DecisionSide side){
        Integer count;

        if(side == null){
            count = decisionRepository.getAllDecisionCount(invitationId);
        } else if(side.equals(DecisionSide.GROOM)){
            count = decisionRepository.getGroomDecisionCount(invitationId);
        } else if(side.equals(DecisionSide.BRIDE)){
            count = decisionRepository.getBrideDecisionCount(invitationId);
        } else {
            throw new CommonException(ErrorCode.EMPTY_FIELD);
        }
        return CountDTO.of(count);
    }

}
