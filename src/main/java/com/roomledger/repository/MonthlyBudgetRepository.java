package com.roomledger.repository;

import com.roomledger.entity.MonthlyBudget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MonthlyBudgetRepository extends JpaRepository<MonthlyBudget, Long> {

    Optional<MonthlyBudget> findByRoomIdAndMonth(Long roomId, String month);

    List<MonthlyBudget> findByRoomIdOrderByMonthDesc(Long roomId);

    // Get the most recent month that has a budget set BEFORE the given month
    @Query("SELECT mb FROM MonthlyBudget mb WHERE mb.room.id = :roomId AND mb.month < :month ORDER BY mb.month DESC")
    List<MonthlyBudget> findPreviousMonths(@Param("roomId") Long roomId, @Param("month") String month);
}