package com.roomledger.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MonthStat {
    private String month;
    private Double total;
    private Long count;
}
