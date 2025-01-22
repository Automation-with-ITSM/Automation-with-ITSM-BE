package com.wedit.weditapp.domain.bankAccounts.domain;

import com.wedit.weditapp.domain.invitation.domain.Invitation;
import com.wedit.weditapp.domain.shared.AccountSide;
import com.wedit.weditapp.domain.shared.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "bankAccounts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BankAccounts extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "side", nullable = false)
    private AccountSide side;

    @Column(nullable = false)
    private String accountNumber;

    @Column(nullable = false)
    private String bankName;

    @Column(nullable = false)
    private String accountHolder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invitation_id", nullable = false)
    private Invitation invitation;

    @Builder
    private BankAccounts(AccountSide side, String accountNumber, String bankName, String accountHolder, Invitation invitation) {
        this.side = side;
        this.accountNumber = accountNumber;
        this.bankName = bankName;
        this.accountHolder = accountHolder;
        this.invitation = invitation;
    }

    public static BankAccounts createBankAccount(AccountSide side, String accountNumber, String bankName, String accountHolder, Invitation invitation) {
        return BankAccounts.builder()
            .side(side)
            .accountNumber(accountNumber)
            .bankName(bankName)
            .accountHolder(accountHolder)
            .invitation(invitation)
            .build();
    }

}
