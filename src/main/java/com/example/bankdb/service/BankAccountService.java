package com.example.bankdb.service;

import com.example.bankdb.exception.AccountNotFoundException;
import com.example.bankdb.exception.UserNotFoundException;
import com.example.bankdb.model.dto.BankAccountDto;
import com.example.bankdb.model.dto.CardSummaryDto;
import com.example.bankdb.model.dto.CreateAccountRequest;
import com.example.bankdb.model.entity.BankAccount;
import com.example.bankdb.model.entity.enums.CurrencyType;
import com.example.bankdb.model.entity.User;
import com.example.bankdb.repository.BankAccountRepository;
import com.example.bankdb.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BankAccountService {

    private final BankAccountRepository bankAccountRepository;

    private final UserRepository userRepository;

    public String createAccount(CreateAccountRequest request) {
        log.info("Creating bank account for username: {}", request.getUsername());
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> {
                    log.error("User not found.: {}", request.getUsername());
                    return new UserNotFoundException("User not found");
                });
        BankAccount account = new BankAccount();
        account.setAccountNumber(UUID.randomUUID().toString());
        account.setAccountType(request.getAccountType());
        account.setBalance(request.getInitialBalance());
        account.setCurrencyType(CurrencyType.AZN);
        account.setOwner(user);
        switch (request.getAccountType()) {
            case DEBIT -> {
                account.setCreditLimit(BigDecimal.ZERO);
                account.setInterestRate(BigDecimal.ZERO);
                account.setDepositTermInMonths(0);
                account.setLocked(false);
            }
            case CREDIT -> {
                account.setCreditLimit(new BigDecimal("4000.00"));
                account.setInterestRate(new BigDecimal("0.08"));
                account.setDepositTermInMonths(0);
                account.setLocked(false);
            }
            case DEPOSIT -> {
                account.setCreditLimit(BigDecimal.ZERO);
                account.setInterestRate(new BigDecimal("0.04"));
                account.setDepositTermInMonths(12);
                account.setLocked(true);

                LocalDate now = LocalDate.now();
                account.setDepositStartDate(now);
                account.setDepositUnlockDate(now.plusMonths(12));
            }
        }

        bankAccountRepository.save(account);
        log.info("Bank account created successfully for username: {}, account number: {}", request.getUsername(), account.getAccountNumber());

        return "Bank account created succesfully";
    }

    public List<BankAccount> getAccountByUsername(String username) {
        log.info("Fetching bank accounts for username: {}", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.error("User not found: {}", username);
                    return new UserNotFoundException("User not found");
                });
        return bankAccountRepository.findByOwnerId(user.getId());
    }

    public void deleteAccount(Long accountId) {
        log.info("Deleting bank account with id: {}", accountId);
        if (!bankAccountRepository.existsById(accountId)) {
            log.error("Account not found with ID.: {}", accountId);
            if (!bankAccountRepository.existsById(accountId)) {
                throw new AccountNotFoundException("Account with ID " + accountId + " not found");
            }
        }
        bankAccountRepository.deleteById(accountId);
        log.info("Bank account with id: {} deleted successfully", accountId);
    }

    public BankAccount updateAccount(Long accountId, BankAccount updatedAccount) {
        log.info("Updating bank account with id: {}", accountId);
        BankAccount existing = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> {
                    log.error("Account not found with ID: {}", accountId);
                    log.error("Account not found with ID: {}", accountId);
                    return new AccountNotFoundException("Account with ID " + accountId + " not found");
                });

        existing.setBalance(updatedAccount.getBalance());
        existing.setCreditLimit(updatedAccount.getCreditLimit());
        existing.setInterestRate(updatedAccount.getInterestRate());
        existing.setDepositTermInMonths(updatedAccount.getDepositTermInMonths());
        existing.setLocked(updatedAccount.isLocked());
        existing.setDepositStartDate(updatedAccount.getDepositStartDate());
        existing.setDepositUnlockDate(updatedAccount.getDepositUnlockDate());
        existing.setCurrencyType(updatedAccount.getCurrencyType());
        existing.setAccountType(updatedAccount.getAccountType());

        BankAccount saved = bankAccountRepository.save(existing);
        log.info("Bank account with id: {} updated successfully", accountId);
        return saved;
    }

    private BankAccountDto mapToDto(BankAccount account) {
        BankAccountDto dto = new BankAccountDto();
        dto.setId(account.getId());
        dto.setAccountNumber(account.getAccountNumber());
        dto.setBalance(account.getBalance());
        dto.setCreditLimit(account.getCreditLimit());
        dto.setInterestRate(account.getInterestRate());
        dto.setDepositTermInMonths(account.getDepositTermInMonths());
        dto.setDepositStartDate(account.getDepositStartDate());
        dto.setDepositUnlockDate(account.getDepositUnlockDate());
        dto.setCurrencyType(account.getCurrencyType());
        dto.setAccountType(account.getAccountType());
        dto.setLocked(account.isLocked());

        dto.setOwnerUsername(account.getOwner().getUsername());

        dto.setCards(account.getCards().stream().map(card -> {
            CardSummaryDto c = new CardSummaryDto();
            c.setId(card.getId());
            c.setCardNumber(card.getCardNumber());
            c.setExpiryDate(card.getExpiryDate());
            return c;
        }).toList());

        return dto;
    }


    public List<BankAccountDto> getAccountDtosByUsername(String username) {
        List<BankAccount> accounts = getAccountByUsername(username);
        return accounts.stream()
                .map(this::mapToDto)
                .toList();
    }


}
