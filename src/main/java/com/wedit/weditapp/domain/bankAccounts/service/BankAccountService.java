package com.wedit.weditapp.domain.bankAccounts.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.wedit.weditapp.domain.bankAccounts.domain.BankAccount;
import com.wedit.weditapp.domain.bankAccounts.domain.repository.BankAccountRepository;
import com.wedit.weditapp.domain.bankAccounts.dto.BankAccountDTO;
import com.wedit.weditapp.domain.invitation.domain.Invitation;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class BankAccountService {
	private final BankAccountRepository bankAccountRepository;

	// DTO 리스트를 엔티티 리스트로 변환하여 저장
	public List<BankAccount> createBankAccounts(List<BankAccountDTO> bankAccountDTOs, Invitation invitation) {
		List<BankAccount> bankAccounts = bankAccountDTOs.stream()
			.map(dto -> BankAccount.createBankAccount(
				dto.getSide(),
				dto.getAccountNumber(),
				dto.getBankName(),
				dto.getAccountHolder(),
				invitation
			)).collect(Collectors.toList());

		return bankAccountRepository.saveAll(bankAccounts);
	}
}
