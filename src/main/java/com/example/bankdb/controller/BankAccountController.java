package com.example.bankdb.controller;

import com.example.bankdb.model.dto.CreateAccountRequest;
import com.example.bankdb.model.entity.BankAccount;
import com.example.bankdb.service.BankAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class BankAccountController {

    private final BankAccountService bankAccountService;

    @PostMapping("/create")
    public String createAccount(@RequestBody CreateAccountRequest request) {
        return bankAccountService.createAccount(request);
    }

    @GetMapping("/{username}")
    public List<BankAccount> getAccounts(@PathVariable String username) {
        return bankAccountService.getAccountByUsername(username);
    }
}
