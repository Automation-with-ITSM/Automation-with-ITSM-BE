package com.wedit.weditapp.domain.invitation.dto.response;

import com.wedit.weditapp.domain.bankAccount.dto.BankAccountDto;
import com.wedit.weditapp.domain.comment.dto.response.CommentResponseDto;
import com.wedit.weditapp.domain.image.dto.response.ImageResponseDto;
import com.wedit.weditapp.domain.invitation.domain.Invitation;
import com.wedit.weditapp.domain.shared.Theme;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
@NoArgsConstructor
public class InvitationResponseDto {

    private Long id;

    private String groom; // 신랑 이름

    private String bride; // 신부 이름

    private String groomF; // 신랑 아버지 이름

    private String groomM; // 신랑 어머니 이름

    private String brideF; // 신부 아버지 이름

    private String brideM; // 신부 어머니 이름

    private String address; // 주소

    private String extraAddress; // 상세 주소

    private LocalDate date; // 결혼식 날짜

    private LocalTime time;

    private Theme theme; // 테마

    private String distribution; // 청첩장 URL

    private boolean guestBookOption; // 방명록 옵션

    private boolean decisionOption; // 참석 여부 옵션

    private boolean accountOption; // 계좌 공개 옵션

    private List<BankAccountDto> bankAccounts; // 계좌 정보 리스트

    private List<ImageResponseDto> image;

    private List<CommentResponseDto> comment; // 방명록 리스트

    @Builder
    private InvitationResponseDto(Long id, String groom, String bride, String groomF, String groomM, String brideF, String brideM, String address, String extraAddress, LocalDate date, LocalTime time, Theme theme, String distribution, boolean guestBookOption, boolean decisionOption, boolean accountOption, List<BankAccountDto> bankAccounts, List<ImageResponseDto> image, List<CommentResponseDto> comment){
        this.id = id;
        this.groom = groom;
        this.bride = bride;
        this.groomF = groomF;
        this.groomM = groomM;
        this.brideF = brideF;
        this.brideM = brideM;
        this.address = address;
        this.extraAddress = extraAddress;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.distribution = distribution;
        this.guestBookOption = guestBookOption;
        this.decisionOption = decisionOption;
        this.accountOption = accountOption;
        this.bankAccounts = bankAccounts;
        this.image = image;
        this.comment = comment;
    }

    public static InvitationResponseDto of(Invitation invitation, List<BankAccountDto> bankAccounts, List<ImageResponseDto> image, List<CommentResponseDto> comment){
        return InvitationResponseDto.builder()
            .id(invitation.getId())
            .groom(invitation.getGroom())
            .bride(invitation.getBride())
            .groomF(invitation.getGroomF())
            .groomM(invitation.getGroomM())
            .brideF(invitation.getBrideF())
            .brideM(invitation.getBrideM())
            .address(invitation.getAddress())
            .extraAddress(invitation.getExtraAddress())
            .date(invitation.getDate())
            .time(invitation.getTime())
            .theme(invitation.getTheme())
            .distribution(invitation.getDistribution())
            .guestBookOption(invitation.isGuestBookOption())
            .decisionOption(invitation.isDecisionOption())
            .accountOption(invitation.isAccountOption())
            .bankAccounts(bankAccounts)
            .image(image)
            .comment(comment)
            .build();
    }
}
