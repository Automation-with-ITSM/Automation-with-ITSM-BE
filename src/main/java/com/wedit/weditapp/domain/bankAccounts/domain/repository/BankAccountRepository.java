package com.wedit.weditapp.domain.bankAccounts.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.wedit.weditapp.domain.bankAccounts.domain.BankAccounts;

public interface BankAccountRepository extends JpaRepository<BankAccounts, Long> {
}

