package com.wedit.weditapp.domain.bankAccounts.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.wedit.weditapp.domain.bankAccounts.domain.BankAccounts;
import com.wedit.weditapp.domain.bankAccounts.domain.repository.BankAccountRepository;
import com.wedit.weditapp.domain.bankAccounts.dto.BankAccountDto;
import com.wedit.weditapp.domain.invitation.domain.Invitation;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class BankAccountService {
	private final BankAccountRepository bankAccountRepository;

	// DTO 리스트를 엔티티 리스트로 변환하여 저장
	public List<BankAccounts> createBankAccounts(List<BankAccountDto> bankAccountDtos, Invitation invitation) {
		List<BankAccounts> bankAccounts = bankAccountDtos.stream()
			.map(dto -> BankAccounts.createBankAccount(
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
		List<BankAccounts> bankAccounts = bankAccountRepository.findByInvitation(invitation);

		return bankAccounts.stream() // 스트림 생성
			.map(BankAccountDto::from) // entity -> DTO
			.collect(Collectors.toList()); // 리스트로 수집
	}
}
