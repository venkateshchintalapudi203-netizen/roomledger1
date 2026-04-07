package com.roomledger.dto;

import lombok.*;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StatsDto {
    private List<CategoryStat> byCategory;
    private List<MonthStat> byMonth;
    private List<MemberStat> byMember;
}
