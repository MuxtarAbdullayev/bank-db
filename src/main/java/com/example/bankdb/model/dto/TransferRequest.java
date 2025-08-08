package com.example.bankdb.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransferRequest {

    @Schema(description = "Sender account ID", example = "1", required = true)
    private Long sourceAccountId;

    @Schema(description = "Receiver account ID", example = "2", required = true)
    private Long destinationAccountId;

    @Schema(description = "Amount to transfer", example = "300.00", required = true)
    private BigDecimal amount;

    @Schema(description = "Transfer description", example = "Rent payment")
    private String description;
}

