package com.example.bankdb.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CardToCardRequest {

    @Schema(description = "Source card ID", example = "10", required = true)
    private Long sourceCardId;

    @Schema(description = "Destination card ID", example = "12", required = true)
    private Long destinationCardId;

    @Schema(description = "Amount to transfer", example = "120.00", required = true)
    private BigDecimal amount;

    @Schema(description = "Transfer description", example = "Friend payback")
    private String description;
}

