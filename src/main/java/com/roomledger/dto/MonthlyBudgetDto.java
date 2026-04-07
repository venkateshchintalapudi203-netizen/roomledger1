package com.roomledger.dto;

import lombok.*;

@Data @AllArgsConstructor @NoArgsConstructor @Builder
public class MonthlyBudgetDto {
    private Long id;
    private String month;
    private Double baseBudget;
    private Double carriedOver;
    private Double totalBudget;
    private Double totalSpent;
    private Double remaining;
    private String monthLabel;   // e.g. "March 2025"
}