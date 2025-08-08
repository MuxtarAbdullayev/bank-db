package com.example.bankdb.model.dto;

import com.example.bankdb.model.entity.enums.AccountType;
import com.example.bankdb.model.entity.enums.CurrencyType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class BankAccountDto {
    private Long id;
    private String accountNumber;
    private BigDecimal balance;
    private BigDecimal creditLimit;
    private BigDecimal interestRate;
    private Integer depositTermInMonths;
    private LocalDate depositStartDate;
    private LocalDate depositUnlockDate;
    private CurrencyType currencyType;
    private AccountType accountType;
    private boolean locked;

    private String ownerUsername;

    private List<CardSummaryDto> cards;
}

