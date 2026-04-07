package com.roomledger.dto;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExpenseDto {
    private Long id;
    private String title;
    private Double amount;
    private String category;
    private String description;
    private LocalDate date;
    private UserDto user;
    private LocalDateTime createdAt;
}
