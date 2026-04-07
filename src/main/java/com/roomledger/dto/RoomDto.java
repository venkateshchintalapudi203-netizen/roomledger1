package com.roomledger.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data @AllArgsConstructor @NoArgsConstructor @Builder
public class RoomDto {
    private Long id;
    private String name;
    private String description;
    private Double budget;        // totalBudget = baseBudget + carriedOver
    private Double baseBudget;
    private Double carriedOver;
    private Double totalSpent;
    private Double remaining;
    private Long expenseCount;
    private UserDto owner;
    private List<UserDto> members;
    private LocalDateTime createdAt;
}