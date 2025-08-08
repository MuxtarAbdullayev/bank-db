package com.example.bankdb.service;

import com.example.bankdb.exception.CardNotFoundException;
import com.example.bankdb.exception.InsufficientFundsException;
import com.example.bankdb.exception.InvalidTransactionRequestException;
import com.example.bankdb.model.dto.TransactionDto;
import com.example.bankdb.model.dto.TransactionRequest;
import com.example.bankdb.model.entity.BankAccount;
import com.example.bankdb.model.entity.Card;
import com.example.bankdb.model.entity.Transaction;
import com.example.bankdb.model.entity.enums.TransactionType;
import com.example.bankdb.repository.BankAccountRepository;
import com.example.bankdb.repository.CardRepository;
import com.example.bankdb.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.security.auth.login.AccountNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final BankAccountRepository bankAccountRepository;
    private final TransactionRepository transactionRepository;
    private final CardRepository cardRepository;

    // Balans yoxlama ve guncelleme
    private void updateBalances(BankAccount from, BankAccount to, BigDecimal amount) {
        if (from.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Insufficient funds");
        }
        from.setBalance(from.getBalance().subtract(amount));
        to.setBalance(to.getBalance().add(amount));

        bankAccountRepository.save(from);
        bankAccountRepository.save(to);
        log.info("Updated balances: fromAccountId={}, toAccountId={}, amount={}", from.getId(), to.getId(), amount);
    }

    private Transaction createTransaction(BankAccount fromAccount, BankAccount toAccount,
                                          Card fromCard, Card toCard,
                                          BigDecimal amount, String description, TransactionType type) {
        Transaction ta = new Transaction();
        ta.setFromAccount(fromAccount);
        ta.setToAccount(toAccount);
        ta.setFromCard(fromCard);
        ta.setToCard(toCard);
        ta.setAmount(amount);
        ta.setDescription(description);
        ta.setTransactionType(type);
        ta.setTimestamp(LocalDateTime.now());

        Transaction saved = transactionRepository.save(ta);
        log.info("Transaction created: id={}, type={}, amount={}", saved.getId(), type, amount);
        return saved;
    }

    public String topUp(Long accountId, BigDecimal amount, String description) {
        BankAccount account = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> {
                    log.error("Top-up failed: account not found with id={}", accountId);
                    return new RuntimeException("Account not found");
                });

        account.setBalance(account.getBalance().add(amount));
        bankAccountRepository.save(account);

        createTransaction(null, account, null, null, amount, description, TransactionType.DEPOSIT);
        log.info("Top-up successful: accountId={}, amount={}", accountId, amount);
        return "Top-up successful";
    }

    public String transfer(Long fromAccountId, Long toAccountId, BigDecimal amount, String description) {
        BankAccount fromAccount = bankAccountRepository.findById(fromAccountId)
                .orElseThrow(() -> new RuntimeException("Sender account not found"));
        BankAccount toAccount = bankAccountRepository.findById(toAccountId)
                .orElseThrow(() -> new RuntimeException("Receiver account not found"));

        updateBalances(fromAccount, toAccount, amount);

        createTransaction(fromAccount, toAccount, null, null, amount, description, TransactionType.TRANSFER);

        return "Transfer successful";
    }

    public List<TransactionDto> getTransactionHistoryByAccountId(Long accountId) {
        List<Transaction> transactions = transactionRepository.findByFromAccountIdOrToAccountId(accountId, accountId);
        return transactions.stream()
                .map(this::convertToDTO)
                .toList();
    }

    @Transactional
    public Transaction processCardToCardTransaction(TransactionRequest request) {
        log.info("Processing card-to-card transaction: fromCard={}, toCard={}, amount={}",
                request.getSourceCardId(), request.getDestinationCardId(), request.getAmount());

        Card fromCard = cardRepository.findById(request.getSourceCardId())
                .orElseThrow(() -> new CardNotFoundException("Source card not found"));
        Card toCard = cardRepository.findById(request.getDestinationCardId())
                .orElseThrow(() -> new CardNotFoundException("Destination card not found"));

        BankAccount fromAccount = fromCard.getLinkedAccount();
        BankAccount toAccount = toCard.getLinkedAccount();

        updateBalances(fromAccount, toAccount, request.getAmount());

        return createTransaction(fromAccount, toAccount, fromCard, toCard,
                request.getAmount(), request.getDescription(), TransactionType.TRANSFER);
    }

    public TransactionDto convertToDTO(Transaction transaction) {
        TransactionDto dto = new TransactionDto();
        dto.setId(transaction.getId());
        dto.setAmount(transaction.getAmount());
        dto.setDescription(transaction.getDescription());
        dto.setTimestamp(transaction.getTimestamp());
        dto.setTransactionType(transaction.getTransactionType().name());

        if (transaction.getFromAccount() != null) {
            dto.setFromAccountId(transaction.getFromAccount().getId());
        }
        if (transaction.getToAccount() != null) {
            dto.setToAccountId(transaction.getToAccount().getId());
        }
        if (transaction.getFromCard() != null) {
            dto.setFromCardId(transaction.getFromCard().getId());
        }
        if (transaction.getToCard() != null) {
            dto.setToCardId(transaction.getToCard().getId());
        }

        return dto;
    }


    @Transactional
    public Transaction processTransaction(TransactionRequest request) throws AccountNotFoundException {
        log.info("Processing transaction: type={}, amount={}", request.getType(), request.getAmount());

        BankAccount source = null;
        BankAccount destination = null;

        if (request.getSourceAccountId() != null) {
            source = bankAccountRepository.findById(request.getSourceAccountId())
                    .orElseThrow(() -> new AccountNotFoundException("Source account not found."));
        }

        if (request.getDestinationAccountId() != null) {
            destination = bankAccountRepository.findById(request.getDestinationAccountId())
                    .orElseThrow(() -> new AccountNotFoundException("Destination account not found."));
        }

        BigDecimal amount = request.getAmount();
        TransactionType type = request.getType();
        String description = request.getDescription();

        Transaction transaction;
        switch (type) {
            case DEPOSIT -> {
                if (destination == null)
                    throw new IllegalArgumentException("Destination account required for deposit");
                destination.setBalance(destination.getBalance().add(amount));
                bankAccountRepository.save(destination);
                transaction = createTransaction(null, destination, null, null, amount, description, type);
            }
            case WITHDRAW -> {
                if (source == null)
                    throw new InvalidTransactionRequestException("Source account required for withdraw");
                if (source.getBalance().compareTo(amount) < 0)
                    throw new InsufficientFundsException("Insufficient funds");
                source.setBalance(source.getBalance().subtract(amount));
                bankAccountRepository.save(source);
                transaction = createTransaction(source, null, null, null, amount, description, type);
            }
            case TRANSFER -> {
                if (source == null || destination == null)
                    throw new InvalidTransactionRequestException("Both accounts required for transfer");
                updateBalances(source, destination, amount);
                transaction = createTransaction(source, destination, null, null, amount, description, type);
            }
            default -> throw new InvalidTransactionRequestException("Unsupported transaction type");
        }
        return transaction;
    }

    public Transaction getTransactionById(Long id) throws AccountNotFoundException {
        return transactionRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Transaction not found with id={}", id);
                    return new com.example.bankdb.exception.AccountNotFoundException("Transaction not found with id: " + id);
                });
    }
}
