package com.example.bankdb.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "bank_accounts")
public class BankAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String accountNumber;

    private BigDecimal balance;

    @Enumerated(EnumType.STRING) //data bazada bu enum adi metn kimi qalir.
    private AccountType accountType;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User owner;
}
