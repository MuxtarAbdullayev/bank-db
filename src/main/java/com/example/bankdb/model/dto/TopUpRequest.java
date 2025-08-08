package com.example.bankdb.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TopUpRequest {

    @Schema(description = "ID of the destination account", example = "1")
    private Long destinationAccountId;

    @Schema(description = "Amount to deposit", example = "250.00")
    private BigDecimal amount;

    @Schema(description = "Optional description for the top-up", example = "Mobile app deposit")
    private String description;
}

