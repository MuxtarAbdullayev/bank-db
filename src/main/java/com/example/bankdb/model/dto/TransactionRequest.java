package com.example.bankdb.model.dto;

import com.example.bankdb.model.entity.enums.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransactionRequest {
    private Long sourceAccountId;
    private Long destinationAccountId;
    private Long sourceCardId;
    private Long destinationCardId;
    private BigDecimal amount;


    @Schema(description = "Transaction type: DEPOSIT, WITHDRAW, TRANSFER")
    private TransactionType type;
    private String description;
}
