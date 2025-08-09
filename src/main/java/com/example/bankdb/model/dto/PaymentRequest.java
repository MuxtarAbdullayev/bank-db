package com.example.bankdb.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentRequest {

    @Schema(description = "ID of the source account", example = "1")
    private Long sourceAccountId;

    @Schema(description = "Amount to withdraw", example = "100.00")
    private BigDecimal amount;

    @Schema(description = "Optional description for the payment", example = "ATM withdrawal")
    private String description;
}

