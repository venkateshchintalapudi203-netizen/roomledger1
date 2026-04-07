package com.roomledger.controller;

import com.roomledger.dto.*;
import com.roomledger.entity.User;
import com.roomledger.repository.UserRepository;
import com.roomledger.service.ExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Expenses", description = "Add, view, edit, delete expenses and get analytics")
@SecurityRequirement(name = "Bearer Authentication")
public class ExpenseController {

    private final ExpenseService expenseService;
    private final UserRepository userRepository;

    private User getUser(UserDetails ud) {
        return userRepository.findByEmail(ud.getUsername()).orElseThrow();
    }

    @GetMapping("/rooms/{roomId}/expenses")
    @Operation(summary = "Get expenses", description = "Filter by optional month (YYYY-MM) and/or category")
    public ResponseEntity<List<ExpenseDto>> getExpenses(
            @PathVariable Long roomId,
            @RequestParam(required = false) String month,
            @RequestParam(required = false) String category,
            @AuthenticationPrincipal UserDetails ud) {
        return ResponseEntity.ok(expenseService.getExpenses(roomId, month, category, getUser(ud)));
    }

    @PostMapping("/rooms/{roomId}/expenses")
    @Operation(summary = "Add an expense")
    public ResponseEntity<ExpenseDto> createExpense(
            @PathVariable Long roomId,
            @Valid @RequestBody CreateExpenseRequest req,
            @AuthenticationPrincipal UserDetails ud) {
        return ResponseEntity.ok(expenseService.createExpense(roomId, req, getUser(ud)));
    }

    @PutMapping("/expenses/{id}")
    @Operation(summary = "Edit an expense", description = "Only the creator or room owner can edit")
    public ResponseEntity<ExpenseDto> updateExpense(
            @Parameter(description = "Expense ID") @PathVariable Long id,
            @Valid @RequestBody UpdateExpenseRequest req,
            @AuthenticationPrincipal UserDetails ud) {
        return ResponseEntity.ok(expenseService.updateExpense(id, req, getUser(ud)));
    }

    @DeleteMapping("/expenses/{id}")
    @Operation(summary = "Delete an expense", description = "Only the creator or room owner can delete")
    public ResponseEntity<Map<String, Boolean>> deleteExpense(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails ud) {
        expenseService.deleteExpense(id, getUser(ud));
        return ResponseEntity.ok(Map.of("success", true));
    }

    @GetMapping("/rooms/{roomId}/stats")
    @Operation(summary = "Get analytics", description = "Pass optional ?month=YYYY-MM for monthly breakdown")
    public ResponseEntity<StatsDto> getStats(
            @PathVariable Long roomId,
            @RequestParam(required = false) String month,
            @AuthenticationPrincipal UserDetails ud) {
        return ResponseEntity.ok(expenseService.getStats(roomId, month, getUser(ud)));
    }
}