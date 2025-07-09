package com.example.bankdb.repository;

import com.example.bankdb.model.entity.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {
    List<BankAccount> findByOwnerId(Long userId);
}
