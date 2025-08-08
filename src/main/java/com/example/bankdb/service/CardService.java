package com.example.bankdb.service;

import com.example.bankdb.exception.AccountNotFoundException;
import com.example.bankdb.exception.CardLimitExceededException;
import com.example.bankdb.exception.CardNotFoundException;
import com.example.bankdb.exception.InvalidCardNumberException;
import com.example.bankdb.model.dto.CardDto;
import com.example.bankdb.model.dto.CreateCardRequest;
import com.example.bankdb.model.entity.BankAccount;
import com.example.bankdb.model.entity.Card;
import com.example.bankdb.repository.BankAccountRepository;
import com.example.bankdb.repository.CardRepository;
import com.example.bankdb.util.CardValidator;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CardService {

    private final CardRepository cardRepository;
    private final BankAccountRepository bankAccountRepository;

    public String createCard(CreateCardRequest request, String userEmail) {
        log.info("Create card for accountId: {} by userEmail: {}", request.getAccountId(), userEmail);
        BankAccount account = bankAccountRepository.findById(request.getAccountId())
                .orElseThrow(() -> {
                    log.error("Account not found with id: {}", request.getAccountId());
                    return new AccountNotFoundException("Account not found");
                });

        if (!account.getOwner().getEmail().equals(userEmail)) {
            log.error("Access denied. User: {} does not own account: {}", userEmail, request.getAccountId());
            throw new AccessDeniedException("You do not own this account");
        }

        long cardCount = cardRepository.countByLinkedAccount_Id(account.getId());
        if (cardCount >= 2) {
            log.error("Card creation failed. Account id: {} already has {} cards", account.getId(), cardCount);
            throw new CardLimitExceededException("Maximum 2 cards allowed per account");
        }

        if (!CardValidator.isValidCardNumber(request.getCardNumber())) {
            log.error("Invalid card number provided: {}", request.getCardNumber());
            throw new InvalidCardNumberException("Invalid card number.");
        }

        Card card = new Card();
        card.setCardNumber(request.getCardNumber());
        card.setExpiryDate(request.getExpiryDate().toString());
        card.setCvv(request.getCvv());
        card.setLinkedAccount(account);

        cardRepository.save(card);
        log.info("Card created successfully for accountId: {}", request.getAccountId());
        return "Card created successfully";
    }

    public List<CardDto> getCardsByAccount(Long accountId) {
        log.info("Fetching cards for accountId: {}", accountId);
        List<Card> cards = cardRepository.findByLinkedAccount_Id(accountId);
        log.info("Found {} cards for accountId: {}", cards.size(), accountId);
        return cards.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private CardDto mapToDto(Card card) {
        CardDto dto = new CardDto();
        dto.setId(card.getId());
        dto.setCardNumber(card.getCardNumber());
        dto.setExpiryDate(card.getExpiryDate());
        return dto;
    }

    public void deleteCard(Long cardId) {
        log.info("Delete card with id: {}", cardId);
        if (!cardRepository.existsById(cardId)) {
            log.error("Card not found with id: {}", cardId);
            throw new CardNotFoundException("Card not found");
        }
        cardRepository.deleteById(cardId);
        log.info("Card deleted successfully with id: {}", cardId);
    }

    public Card updateCard(Long cardId, CreateCardRequest request) {
        log.info("Updating card with id: {}", cardId);
        Card existingCard = cardRepository.findById(cardId)
                .orElseThrow(() -> {
                    log.error("Card not found with id.: {}", cardId);
                    return new EntityNotFoundException("Card not found with id: " + cardId);
                });

        BankAccount linkedAccount = bankAccountRepository.findById(request.getAccountId())
                .orElseThrow(() -> {
                    log.error("Linked bank account not found with id: {}", request.getAccountId());
                    return new EntityNotFoundException("Bank account not found with id: " + request.getAccountId());
                });

        existingCard.setCardNumber(request.getCardNumber());
        existingCard.setExpiryDate(request.getExpiryDate().toString());
        existingCard.setCvv(request.getCvv());
        existingCard.setLinkedAccount(linkedAccount);

        Card updated = cardRepository.save(existingCard);
        log.info("Card updated successfully with id: {}", cardId);
        return updated;
    }
}
