package com.example.bankdb.model.dto;

import lombok.Data;

@Data
public class CardSummaryDto {
    private Long id;
    private String cardNumber;
    private String expiryDate;
}

