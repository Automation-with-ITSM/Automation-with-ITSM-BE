package com.wedit.weditapp.domain.bankAccounts.domain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.wedit.weditapp.domain.bankAccounts.domain.BankAccount;
import com.wedit.weditapp.domain.invitation.domain.Invitation;

public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {
	List<BankAccount> findByInvitation(Invitation invitation);
}
