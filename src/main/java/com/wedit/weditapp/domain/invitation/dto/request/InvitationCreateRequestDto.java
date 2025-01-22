package com.wedit.weditapp.domain.invitation.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

import com.wedit.weditapp.domain.bankAccounts.dto.BankAccountDto;
import com.wedit.weditapp.domain.shared.Theme;

@Getter
@NoArgsConstructor
public class InvitationCreateRequestDto {
    @NotBlank(message = "groom cannot be blank")
    private String groom; // 신랑 이름

    @NotBlank(message = "bride cannot be blank")
    private String bride; // 신부 이름

    private String groomF; // 신랑 아버지 이름

    private String groomM;  // 신랑 어머니 이름

    private String brideF; // 신부 아버지 이름

    private String brideM; // 신부 어머니 이름

    @NotBlank(message = "address cannot be blank")
    private String address; // 주소

    private String extraAddress; // 상세 주소

    private LocalDate date; // 결혼식 날짜

    private Theme theme; // 테마

    private String distribution; // 청첩장 URL

    private boolean guestBookOption; // 방명록 옵션

    private boolean decisionOption; // 참석 의사 수집 옵션

    private boolean accountOption; // 계좌 공개 옵션

    private List<BankAccountDto> bankAccounts; // 계좌 정보 리스트
}
