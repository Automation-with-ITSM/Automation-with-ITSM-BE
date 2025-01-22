package com.wedit.weditapp.domain.decisions.dto.response;

import com.wedit.weditapp.domain.decisions.domain.Decisions;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CountDTO {
    private Integer count;

    @Builder
    private CountDTO(Integer count){
        this.count = count;
    }

    public static CountDTO of(Integer count){
        return CountDTO.builder()
                .count(count)
                .build();
    }
}
