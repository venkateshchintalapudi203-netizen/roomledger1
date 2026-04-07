package com.roomledger.service;

import com.roomledger.dto.*;
import com.roomledger.entity.Expense;
import com.roomledger.entity.Room;
import com.roomledger.entity.User;
import com.roomledger.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final RoomService roomService;

    public List<ExpenseDto> getExpenses(Long roomId, String month, String category, User user) {
        roomService.getRoomAndCheckAccess(roomId, user);
        List<Expense> expenses;
        if (month != null && category != null) {
            expenses = expenseRepository.findByRoomIdAndMonthAndCategory(roomId, month, category);
        } else if (month != null) {
            expenses = expenseRepository.findByRoomIdAndMonth(roomId, month);
        } else if (category != null) {
            expenses = expenseRepository.findByRoomIdAndCategory(roomId, category);
        } else {
            expenses = expenseRepository.findByRoomIdOrderByDateDescCreatedAtDesc(roomId);
        }
        return expenses.stream().map(this::toDto).collect(Collectors.toList());
    }

    public ExpenseDto createExpense(Long roomId, CreateExpenseRequest req, User user) {
        Room room = roomService.getRoomAndCheckAccess(roomId, user);
        Expense expense = Expense.builder()
                .title(req.getTitle())
                .amount(req.getAmount())
                .category(req.getCategory())
                .description(req.getDescription())
                .date(req.getDate())
                .room(room)
                .user(user)
                .build();
        return toDto(expenseRepository.save(expense));
    }

    public ExpenseDto updateExpense(Long expenseId, UpdateExpenseRequest req, User user) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new RuntimeException("Expense not found"));
        Room room = expense.getRoom();
        boolean isOwner = room.getOwner().getId().equals(user.getId());
        boolean isCreator = expense.getUser().getId().equals(user.getId());
        if (!isOwner && !isCreator) {
            throw new RuntimeException("Access denied");
        }
        expense.setTitle(req.getTitle());
        expense.setAmount(req.getAmount());
        expense.setCategory(req.getCategory());
        expense.setDescription(req.getDescription());
        expense.setDate(req.getDate());
        return toDto(expenseRepository.save(expense));
    }

    public void deleteExpense(Long expenseId, User user) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new RuntimeException("Expense not found"));
        Room room = expense.getRoom();
        boolean isOwner = room.getOwner().getId().equals(user.getId());
        boolean isCreator = expense.getUser().getId().equals(user.getId());
        if (!isOwner && !isCreator) {
            throw new RuntimeException("Access denied");
        }
        expenseRepository.delete(expense);
    }

    public StatsDto getStats(Long roomId, String month, User user) {
        roomService.getRoomAndCheckAccess(roomId, user);

        List<CategoryStat> byCategory = (month != null
                ? expenseRepository.getStatsByCategoryAndMonth(roomId, month)
                : expenseRepository.getStatsByCategory(roomId))
                .stream().map(r -> new CategoryStat((String) r[0], (Double) r[1], (Long) r[2]))
                .collect(Collectors.toList());

        List<MonthStat> byMonth = expenseRepository.getStatsByMonth(roomId).stream()
                .map(r -> new MonthStat((String) r[0], (Double) r[1], (Long) r[2]))
                .collect(Collectors.toList());

        List<MemberStat> byMember = (month != null
                ? expenseRepository.getStatsByMemberAndMonth(roomId, month)
                : expenseRepository.getStatsByMember(roomId))
                .stream().map(r -> new MemberStat((String) r[0], (Double) r[1], (Long) r[2]))
                .collect(Collectors.toList());

        return StatsDto.builder()
                .byCategory(byCategory)
                .byMonth(byMonth)
                .byMember(byMember)
                .build();
    }

    private ExpenseDto toDto(Expense e) {
        return ExpenseDto.builder()
                .id(e.getId())
                .title(e.getTitle())
                .amount(e.getAmount())
                .category(e.getCategory())
                .description(e.getDescription())
                .date(e.getDate())
                .user(new UserDto(e.getUser().getId(), e.getUser().getName(), e.getUser().getEmail()))
                .createdAt(e.getCreatedAt())
                .build();
    }
}