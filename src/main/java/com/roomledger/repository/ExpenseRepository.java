package com.roomledger.repository;

import com.roomledger.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findByRoomIdOrderByDateDescCreatedAtDesc(Long roomId);

    @Query("SELECT e FROM Expense e WHERE e.room.id = :roomId AND FUNCTION('FORMATDATETIME', e.date, 'yyyy-MM') = :month ORDER BY e.date DESC")
    List<Expense> findByRoomIdAndMonth(@Param("roomId") Long roomId, @Param("month") String month);

    @Query("SELECT e FROM Expense e WHERE e.room.id = :roomId AND e.category = :category ORDER BY e.date DESC")
    List<Expense> findByRoomIdAndCategory(@Param("roomId") Long roomId, @Param("category") String category);

    @Query("SELECT e FROM Expense e WHERE e.room.id = :roomId AND FUNCTION('FORMATDATETIME', e.date, 'yyyy-MM') = :month AND e.category = :category ORDER BY e.date DESC")
    List<Expense> findByRoomIdAndMonthAndCategory(@Param("roomId") Long roomId, @Param("month") String month, @Param("category") String category);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.room.id = :roomId")
    Double getTotalSpentByRoom(@Param("roomId") Long roomId);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.room.id = :roomId AND FUNCTION('FORMATDATETIME', e.date, 'yyyy-MM') = :month")
    Double getTotalSpentByRoomAndMonth(@Param("roomId") Long roomId, @Param("month") String month);

    // Stats - all time
    @Query("SELECT e.category, SUM(e.amount), COUNT(e) FROM Expense e WHERE e.room.id = :roomId GROUP BY e.category ORDER BY SUM(e.amount) DESC")
    List<Object[]> getStatsByCategory(@Param("roomId") Long roomId);

    @Query("SELECT FUNCTION('FORMATDATETIME', e.date, 'yyyy-MM'), SUM(e.amount), COUNT(e) FROM Expense e WHERE e.room.id = :roomId GROUP BY FUNCTION('FORMATDATETIME', e.date, 'yyyy-MM') ORDER BY FUNCTION('FORMATDATETIME', e.date, 'yyyy-MM') DESC")
    List<Object[]> getStatsByMonth(@Param("roomId") Long roomId);

    @Query("SELECT u.name, SUM(e.amount), COUNT(e) FROM Expense e JOIN e.user u WHERE e.room.id = :roomId GROUP BY u.id, u.name ORDER BY SUM(e.amount) DESC")
    List<Object[]> getStatsByMember(@Param("roomId") Long roomId);

    // Stats - filtered by month
    @Query("SELECT e.category, SUM(e.amount), COUNT(e) FROM Expense e WHERE e.room.id = :roomId AND FUNCTION('FORMATDATETIME', e.date, 'yyyy-MM') = :month GROUP BY e.category ORDER BY SUM(e.amount) DESC")
    List<Object[]> getStatsByCategoryAndMonth(@Param("roomId") Long roomId, @Param("month") String month);

    @Query("SELECT u.name, SUM(e.amount), COUNT(e) FROM Expense e JOIN e.user u WHERE e.room.id = :roomId AND FUNCTION('FORMATDATETIME', e.date, 'yyyy-MM') = :month GROUP BY u.id, u.name ORDER BY SUM(e.amount) DESC")
    List<Object[]> getStatsByMemberAndMonth(@Param("roomId") Long roomId, @Param("month") String month);

    long countByRoomId(Long roomId);
}