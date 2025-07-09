package com.example.bankdb.model.dto;

import com.example.bankdb.model.entity.AccountType;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CreateAccountRequest {
    private String username;
    private AccountType accountType;
    private BigDecimal initialBalance;
}
