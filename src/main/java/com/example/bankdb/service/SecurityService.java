package com.example.bankdb.service;

import com.example.bankdb.repository.BankAccountRepository;
import com.example.bankdb.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service("securityService")
@RequiredArgsConstructor
public class SecurityService {

    private final BankAccountRepository bankAccountRepository;
    private final CardRepository cardRepository;

    public boolean isAccountOwner(Authentication authentication, Long accountId) {
        String email = authentication.getName();

        return bankAccountRepository.findById(accountId)
                .map(account -> account.getOwner().getEmail().equals(email))
                .orElse(false);
    }

    public boolean isCardOwner(Authentication authentication, Long cardId) {
        String email = authentication.getName();

        return cardRepository.findById(cardId)
                .map(card -> card.getLinkedAccount().getOwner().getEmail().equals(email))
                .orElse(false);
    }
}
