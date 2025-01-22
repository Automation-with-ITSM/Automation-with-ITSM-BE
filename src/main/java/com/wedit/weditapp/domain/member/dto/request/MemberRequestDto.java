package com.wedit.weditapp.domain.member.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor
public class MemberRequestDto {
    @Email(message = "이메일 형식을 지켜야 합니다.")
    @NotBlank(message = "이메일을 반드시 입력해야 합니다.")
    private String email;

    @NotBlank(message = "이름을 반드시 입력해야 합니다.")
    private String name;
}
