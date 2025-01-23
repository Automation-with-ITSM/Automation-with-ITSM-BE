package com.wedit.weditapp.domain.decisions.dto.response;

import com.wedit.weditapp.domain.decisions.domain.Decision;
import com.wedit.weditapp.domain.shared.DecisionSide;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class DecisionResponseDto {
    private Long decisionId;
    private String name;
    private String phoneNumber;
    private Integer addPerson;
    private DecisionSide side;

    @Builder
    private DecisionResponseDto(Long decisionId, String name, String phoneNumber, Integer addPerson, DecisionSide side) {
        this.decisionId = decisionId;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.addPerson = addPerson;
        this.side = side;
    }

    public static DecisionResponseDto from(Decision decision) {
        return DecisionResponseDto.builder()
                .decisionId(decision.getId())
                .name(decision.getName())
                .phoneNumber(decision.getPhoneNumber())
                .addPerson(decision.getAddPerson())
                .side(decision.getSide())
                .build();
    }
}
