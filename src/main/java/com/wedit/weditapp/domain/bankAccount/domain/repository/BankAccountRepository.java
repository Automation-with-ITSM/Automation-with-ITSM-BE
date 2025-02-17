package com.wedit.weditapp.domain.bankAccount.domain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.wedit.weditapp.domain.bankAccount.domain.BankAccount;
import com.wedit.weditapp.domain.invitation.domain.Invitation;
import com.wedit.weditapp.domain.shared.AccountSide;

public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {
	List<BankAccount> findByInvitation(Invitation invitation);

	BankAccount findByInvitationAndSide(Invitation invitation, AccountSide side);
}
