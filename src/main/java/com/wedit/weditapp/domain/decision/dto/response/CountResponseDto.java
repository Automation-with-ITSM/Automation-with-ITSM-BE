package com.wedit.weditapp.domain.decision.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CountResponseDto {
    private Integer totalCount;
    private Integer groomCount;
    private Integer brideCount;

    @Builder
    private CountResponseDto(Integer totalCount, Integer groomCount, Integer brideCount){
        this.totalCount = totalCount;
        this.groomCount = groomCount;
        this.brideCount = brideCount;
    }

    public static CountResponseDto of(Integer totalCount, Integer groomCount, Integer brideCount){
        return CountResponseDto.builder()
                .totalCount(totalCount)
                .groomCount(groomCount)
                .brideCount(brideCount)
                .build();
    }
}
