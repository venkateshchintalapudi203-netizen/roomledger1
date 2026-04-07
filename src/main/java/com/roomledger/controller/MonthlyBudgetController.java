package com.roomledger.controller;

import com.roomledger.dto.MonthlyBudgetDto;
import com.roomledger.dto.SetMonthlyBudgetRequest;
import com.roomledger.entity.User;
import com.roomledger.repository.UserRepository;
import com.roomledger.service.MonthlyBudgetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rooms/{roomId}/budgets")
@RequiredArgsConstructor
@Tag(name = "Monthly Budgets", description = "Set per-month budgets with automatic carryover of remaining balance")
@SecurityRequirement(name = "Bearer Authentication")
public class MonthlyBudgetController {

    private final MonthlyBudgetService budgetService;
    private final UserRepository userRepository;

    private User getUser(UserDetails ud) {
        return userRepository.findByEmail(ud.getUsername()).orElseThrow();
    }

    @GetMapping
    @Operation(summary = "Get all monthly budgets",
        description = "Returns last 12 months with base budget, carryover, total budget, spent, and remaining")
    public ResponseEntity<List<MonthlyBudgetDto>> getAllBudgets(
            @PathVariable Long roomId,
            @AuthenticationPrincipal UserDetails ud) {
        return ResponseEntity.ok(budgetService.getAllMonthlyBudgets(roomId, getUser(ud)));
    }

    @GetMapping("/{month}")
    @Operation(summary = "Get budget for a specific month",
        description = "Returns budget details for YYYY-MM format month")
    public ResponseEntity<MonthlyBudgetDto> getBudget(
            @PathVariable Long roomId,
            @PathVariable String month,
            @AuthenticationPrincipal UserDetails ud) {
        return ResponseEntity.ok(budgetService.getMonthlyBudget(roomId, month, getUser(ud)));
    }

    @PostMapping
    @Operation(summary = "Set budget for a month",
        description = "Sets the base budget for a specific month. Carryover from previous month is added automatically. Only room owner can set budgets.")
    public ResponseEntity<MonthlyBudgetDto> setBudget(
            @PathVariable Long roomId,
            @Valid @RequestBody SetMonthlyBudgetRequest req,
            @AuthenticationPrincipal UserDetails ud) {
        return ResponseEntity.ok(budgetService.setMonthlyBudget(roomId, req, getUser(ud)));
    }
}