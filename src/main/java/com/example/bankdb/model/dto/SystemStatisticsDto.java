package com.example.bankdb.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SystemStatisticsDto {
    private long totalUsers;
    private long totalAccounts;
    private long totalTransactions;
}

