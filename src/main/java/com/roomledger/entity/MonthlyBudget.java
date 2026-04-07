package com.roomledger.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "monthly_budgets",
    uniqueConstraints = @UniqueConstraint(columnNames = {"room_id", "budget_month"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MonthlyBudget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(name = "budget_month", nullable = false)   // ← renamed from "month"
    private String month;

    @Column(nullable = false)
    private Double baseBudget;

    @Column(nullable = false)
    private Double carriedOver;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        if (carriedOver == null) carriedOver = 0.0;
    }

    public Double getTotalBudget() {
        return (baseBudget != null ? baseBudget : 0.0)
             + (carriedOver != null ? carriedOver : 0.0);
    }
}