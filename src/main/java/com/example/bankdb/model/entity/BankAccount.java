package com.example.bankdb.model.entity;

import com.example.bankdb.model.entity.enums.AccountType;
import com.example.bankdb.model.entity.enums.CurrencyType;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

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

    private BigDecimal creditLimit;

    private BigDecimal interestRate;

    private Integer depositTermInMonths;
    private boolean isLocked;

    private LocalDate depositStartDate;

    private LocalDate depositUnlockDate;

    @Enumerated(EnumType.STRING)
    private CurrencyType currencyType;

    @Enumerated(EnumType.STRING) //data bazada bu enum adi metn kimi qalir.
    private AccountType accountType;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonBackReference
    private User owner;

    @OneToMany(mappedBy = "linkedAccount", cascade = CascadeType.ALL)
    private List<Card> cards;

    @OneToMany(mappedBy = "fromAccount", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Transaction> transactions;

    private LocalDate lastInterestAppliedDate;
}
