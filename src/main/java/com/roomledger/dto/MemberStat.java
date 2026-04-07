package com.roomledger.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MemberStat {
    private String name;
    private Double total;
    private Long count;
}
