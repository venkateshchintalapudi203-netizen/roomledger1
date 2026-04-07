package com.roomledger.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryStat {
    private String category;
    private Double total;
    private Long count;
}
