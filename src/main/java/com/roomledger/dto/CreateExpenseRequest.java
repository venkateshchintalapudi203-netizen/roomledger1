package com.roomledger.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;

@Data
public class CreateExpenseRequest {
    @NotBlank private String title;
    @NotNull @Positive private Double amount;
    @NotBlank private String category;
    private String description;
    @NotNull private LocalDate date;
}
