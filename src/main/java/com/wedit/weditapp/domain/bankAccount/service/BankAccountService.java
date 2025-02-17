package com.wedit.weditapp.domain.bankAccount.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.wedit.weditapp.domain.bankAccount.domain.BankAccount;
import com.wedit.weditapp.domain.bankAccount.domain.repository.BankAccountRepository;
import com.wedit.weditapp.domain.bankAccount.dto.BankAccountDto;
import com.wedit.weditapp.domain.invitation.domain.Invitation;
import com.wedit.weditapp.global.error.ErrorCode;
import com.wedit.weditapp.global.error.exception.CommonException;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class BankAccountService {
	private final BankAccountRepository bankAccountRepository;

	// DTO 리스트를 엔티티 리스트로 변환하여 저장
	public List<BankAccount> createBankAccounts(List<BankAccountDto> bankAccountDtos, Invitation invitation) {
		// 계좌 정보 리스트가 비었는지 확인
		if (bankAccountDtos == null || bankAccountDtos.isEmpty()) {
			throw new CommonException(ErrorCode.BANK_ACCOUNT_EMPTY);
		}
		List<BankAccount> bankAccounts = bankAccountDtos.stream()
			.map(dto -> BankAccount.createBankAccount(
				dto.getSide(),
				dto.getAccountNumber(),
				dto.getBankName(),
				dto.getAccountHolder(),
				invitation
			)).collect(Collectors.toList());

		return bankAccountRepository.saveAll(bankAccounts);
	}

	// 특정 초대장 계좌 정보를 조회하여 DTO 리스트로 변환
	public List<BankAccountDto> getBankAccounts(Invitation invitation) {
		List<BankAccount> bankAccounts = bankAccountRepository.findByInvitation(invitation);

		return bankAccounts.stream() // 스트림 생성
			.map(BankAccountDto::from) // entity -> DTO
			.collect(Collectors.toList()); // 리스트로 수집
	}

	// 계좌 정보 수정
	public void updateBankAccount(List<BankAccountDto> bankAccountDtos, Invitation invitation) {
		// 계좌 정보 리스트가 비었는지 확인
		if (bankAccountDtos == null || bankAccountDtos.isEmpty()) {
			throw new CommonException(ErrorCode.BANK_ACCOUNT_EMPTY);
		}

		List<BankAccount> updatedAccounts = bankAccountDtos.stream()
			.map(dto -> {
				BankAccount account = bankAccountRepository.findByInvitationAndSide(invitation, dto.getSide());

				if (account != null) {
					// 기존 계좌 업데이트
					account.updateBankAccount(
						dto.getSide(),
						dto.getAccountNumber(),
						dto.getBankName(),
						dto.getAccountHolder(),
						invitation
					);
				} else {
					// 새 계좌 생성
					account = BankAccount.createBankAccount(
						dto.getSide(),
						dto.getAccountNumber(),
						dto.getBankName(),
						dto.getAccountHolder(),
						invitation
					);
				}
				return account;
			})
			.collect(Collectors.toList());

		// 수정된 계좌 정보 저장
		bankAccountRepository.saveAll(updatedAccounts);
	}

	// 계좌 정보 삭제
	public void deleteBankAccount(Invitation invitation) {
		List<BankAccount> bankAccounts = bankAccountRepository.findByInvitation(invitation);

		if (bankAccounts == null || bankAccounts.isEmpty()) {
			return;
		}

		bankAccountRepository.deleteAll(bankAccounts);
	}
}
