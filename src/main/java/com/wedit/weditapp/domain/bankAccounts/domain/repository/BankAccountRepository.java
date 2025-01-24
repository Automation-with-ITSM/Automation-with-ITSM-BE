package com.wedit.weditapp.domain.bankAccounts.domain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.wedit.weditapp.domain.bankAccounts.domain.BankAccount;
import com.wedit.weditapp.domain.invitation.domain.Invitation;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {
	List<BankAccount> findByInvitation(Invitation invitation);

	@Modifying
	@Query("DELETE FROM BankAccount b WHERE b.invitation.id = :invitationId")
	void deleteAllByInvitationId(@Param("invitationId") Long invitationId);
}
