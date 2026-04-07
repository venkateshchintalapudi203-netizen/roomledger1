package com.roomledger.service;

import com.roomledger.dto.MonthlyBudgetDto;
import com.roomledger.dto.SetMonthlyBudgetRequest;
import com.roomledger.entity.MonthlyBudget;
import com.roomledger.entity.Room;
import com.roomledger.entity.User;
import com.roomledger.repository.ExpenseRepository;
import com.roomledger.repository.MonthlyBudgetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MonthlyBudgetService {

    private final MonthlyBudgetRepository budgetRepository;
    private final ExpenseRepository expenseRepository;
    private final RoomService roomService;

    private static final DateTimeFormatter MONTH_FMT = DateTimeFormatter.ofPattern("yyyy-MM");

    /**
     * Set or update the base budget for a specific month.
     * Automatically calculates carryover from the previous month's remaining balance.
     */
    @Transactional
    public MonthlyBudgetDto setMonthlyBudget(Long roomId, SetMonthlyBudgetRequest req, User user) {
        Room room = roomService.getRoomAndCheckAccess(roomId, user);

        // Only room owner can set budget
        if (!room.getOwner().getId().equals(user.getId())) {
            throw new RuntimeException("Only the room owner can set the budget");
        }

        String month = req.getMonth();
        double carriedOver = calculateCarryover(roomId, month);

        // Upsert: update if exists, create if not
        MonthlyBudget budget = budgetRepository.findByRoomIdAndMonth(roomId, month)
                .orElse(MonthlyBudget.builder().room(room).month(month).build());

        budget.setBaseBudget(req.getBaseBudget());
        budget.setCarriedOver(carriedOver);
        budget = budgetRepository.save(budget);

        // Recalculate future months' carryover (cascade effect)
        recalculateFutureMonths(roomId, month);

        return toDto(budget, roomId);
    }

    /**
     * Get budget info for a specific month.
     * If no budget is set for this month, looks back to find the most recent one and carries it forward.
     */
    public MonthlyBudgetDto getMonthlyBudget(Long roomId, String month, User user) {
        roomService.getRoomAndCheckAccess(roomId, user);

        Optional<MonthlyBudget> existing = budgetRepository.findByRoomIdAndMonth(roomId, month);
        if (existing.isPresent()) {
            return toDto(existing.get(), roomId);
        }

        // No budget set for this month — return empty with carryover info
        double carriedOver = calculateCarryover(roomId, month);
        Double spent = expenseRepository.getTotalSpentByRoomAndMonth(roomId, month);
        if (spent == null) spent = 0.0;

        return MonthlyBudgetDto.builder()
                .month(month)
                .baseBudget(0.0)
                .carriedOver(carriedOver)
                .totalBudget(carriedOver)
                .totalSpent(spent)
                .remaining(carriedOver - spent)
                .monthLabel(formatMonthLabel(month))
                .build();
    }

    /**
     * Get all monthly budgets for a room (last 12 months + future).
     */
    public List<MonthlyBudgetDto> getAllMonthlyBudgets(Long roomId, User user) {
        roomService.getRoomAndCheckAccess(roomId, user);

        // Get last 12 months
        List<String> months = getLast12Months();
        return months.stream()
                .map(month -> getMonthlyBudget(roomId, month, user))
                .collect(Collectors.toList());
    }

    /**
     * Calculate how much carries over from the PREVIOUS month into the given month.
     * Carryover = previous month's (totalBudget - totalSpent), if positive.
     * If previous month had a deficit, carryover is 0 (we don't carry over debt).
     */
    private double calculateCarryover(Long roomId, String month) {
        List<MonthlyBudget> previousMonths = budgetRepository.findPreviousMonths(roomId, month);
        if (previousMonths.isEmpty()) return 0.0;

        // Get the immediately previous month
        MonthlyBudget prevBudget = previousMonths.get(0);
        String prevMonth = prevBudget.getMonth();

        Double prevSpent = expenseRepository.getTotalSpentByRoomAndMonth(roomId, prevMonth);
        if (prevSpent == null) prevSpent = 0.0;

        double prevTotal = prevBudget.getTotalBudget();
        double prevRemaining = prevTotal - prevSpent;

        // Only carry over positive remaining (don't carry over debt)
        return Math.max(0.0, prevRemaining);
    }

    /**
     * When a month's budget changes, recalculate all future months' carryover.
     * This ensures the cascade effect of changing one month ripples forward.
     */
    @Transactional
    private void recalculateFutureMonths(Long roomId, String fromMonth) {
        List<MonthlyBudget> futureBudgets = budgetRepository.findByRoomIdOrderByMonthDesc(roomId)
                .stream()
                .filter(b -> b.getMonth().compareTo(fromMonth) > 0)
                .sorted((a, b) -> a.getMonth().compareTo(b.getMonth())) // ascending
                .collect(Collectors.toList());

        for (MonthlyBudget future : futureBudgets) {
            double newCarryover = calculateCarryover(roomId, future.getMonth());
            future.setCarriedOver(newCarryover);
            budgetRepository.save(future);
        }
    }

    private List<String> getLast12Months() {
        List<String> months = new ArrayList<>();
        LocalDate now = LocalDate.now();
        for (int i = 11; i >= 0; i--) {
            months.add(now.minusMonths(i).format(MONTH_FMT));
        }
        return months;
    }

    private String formatMonthLabel(String month) {
        try {
            LocalDate d = LocalDate.parse(month + "-01");
            return d.format(DateTimeFormatter.ofPattern("MMMM yyyy"));
        } catch (Exception e) {
            return month;
        }
    }

    private MonthlyBudgetDto toDto(MonthlyBudget mb, Long roomId) {
        Double spent = expenseRepository.getTotalSpentByRoomAndMonth(roomId, mb.getMonth());
        if (spent == null) spent = 0.0;
        double total = mb.getTotalBudget();

        return MonthlyBudgetDto.builder()
                .id(mb.getId())
                .month(mb.getMonth())
                .baseBudget(mb.getBaseBudget())
                .carriedOver(mb.getCarriedOver())
                .totalBudget(total)
                .totalSpent(spent)
                .remaining(total - spent)
                .monthLabel(formatMonthLabel(mb.getMonth()))
                .build();
    }
}