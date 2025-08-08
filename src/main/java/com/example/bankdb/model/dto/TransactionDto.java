package com.example.bankdb.model.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TransactionDto {
    private Long id;
    private BigDecimal amount;
    private String description;
    private LocalDateTime timestamp;
    private Long fromAccountId;
    private Long toAccountId;
    private Long fromCardId;
    private Long toCardId;
    private String transactionType;
}
