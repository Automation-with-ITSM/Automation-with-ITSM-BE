package com.wedit.weditapp.domain.bankAccount.dto;

import com.wedit.weditapp.domain.bankAccount.domain.BankAccount;
import com.wedit.weditapp.domain.shared.AccountSide;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BankAccountDto {

	@NotBlank(message = "side cannot be blank")
	private AccountSide side;  // 신랑/신부 구분

	@NotBlank(message = "accountNumber cannot be blank")
	private String accountNumber; // 계좌 번호

	@NotBlank(message = "bankName cannot be blank")
	private String bankName; // 은행 이름

	@NotBlank(message = "accountHolder cannot be blank")
	private String accountHolder; // 예금주 이름

	@Builder
	private BankAccountDto(AccountSide side, String accountNumber, String bankName, String accountHolder) {
		this.side = side;
		this.accountNumber = accountNumber;
		this.bankName = bankName;
		this.accountHolder = accountHolder;
	}

	public static BankAccountDto from(BankAccount bankAccount) {
		return BankAccountDto.builder()
			.side(bankAccount.getSide())
			.accountNumber(bankAccount.getAccountNumber())
			.bankName(bankAccount.getBankName())
			.accountHolder(bankAccount.getAccountHolder())
			.build();
	}
}
