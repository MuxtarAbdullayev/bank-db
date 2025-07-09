package com.example.bankdb.service;

import com.example.bankdb.exception.UserNotFoundException;
import com.example.bankdb.model.dto.CreateAccountRequest;
import com.example.bankdb.model.entity.BankAccount;
import com.example.bankdb.model.entity.User;
import com.example.bankdb.repository.BankAccountRepository;
import com.example.bankdb.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BankAccountService {

    private final BankAccountRepository bankAccountRepository;

    private final UserRepository userRepository;

    public String createAccount(CreateAccountRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        BankAccount account = new BankAccount();
        account.setAccountNumber(UUID.randomUUID().toString());
        account.setAccountType(request.getAccountType());
        account.setBalance(request.getInitialBalance());
        account.setOwner(user);

        bankAccountRepository.save(account);
        return "Bank account created succesfully";
    }

    public List<BankAccount> getAccountByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        return bankAccountRepository.findByOwnerId(user.getId());
    }
}
