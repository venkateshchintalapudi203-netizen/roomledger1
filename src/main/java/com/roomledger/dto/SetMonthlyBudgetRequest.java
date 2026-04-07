package com.roomledger.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class SetMonthlyBudgetRequest {
    @NotBlank
    private String month;        // format: "YYYY-MM"

    @NotNull @PositiveOrZero
    private Double baseBudget;
}