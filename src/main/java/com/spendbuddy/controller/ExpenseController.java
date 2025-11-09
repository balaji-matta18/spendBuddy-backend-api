package com.spendbuddy.controller;

import com.spendbuddy.request.dto.ExpenseRequest;
import com.spendbuddy.response.dto.ExpenseResponse;
import com.spendbuddy.service.ExpenseService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/expense")
public class ExpenseController {

	@Autowired
	private ExpenseService expenseService;

	// ✅ Create expense
	@PostMapping
	public ResponseEntity<ExpenseResponse> createExpense(
			@AuthenticationPrincipal UserDetails userDetails,
			@Valid @RequestBody ExpenseRequest request
	) throws Exception {
		ExpenseResponse savedExpense = expenseService.save(userDetails, request);
		return ResponseEntity.ok(savedExpense);
	}

	// ✅ List all or by date range
	@GetMapping
	public ResponseEntity<List<ExpenseResponse>> listAllExpenses(
			@AuthenticationPrincipal UserDetails userDetails,
			@RequestParam(required = false) String fromDate,
			@RequestParam(required = false) String toDate
	) {
		List<ExpenseResponse> expenses = expenseService.list(userDetails, fromDate, toDate);
		return ResponseEntity.ok(expenses);
	}

	// ✅ List current month expenses
	@GetMapping("/current-month")
	public ResponseEntity<List<ExpenseResponse>> listCurrentMonthExpenses(
			@AuthenticationPrincipal UserDetails userDetails
	) {
		List<ExpenseResponse> expenses = expenseService.listExpenseCurrentMonth(userDetails);
		return ResponseEntity.ok(expenses);
	}

	// ✅ List expenses by budget (optional date filter)
	@GetMapping("/budget/{budgetId}")
	public ResponseEntity<List<ExpenseResponse>> listExpenseByBudget(
			@AuthenticationPrincipal UserDetails userDetails,
			@PathVariable Long budgetId,
			@RequestParam(required = false) String fromDate,
			@RequestParam(required = false) String toDate
	) {
		List<ExpenseResponse> expenses = expenseService.listExpenseByBudget(userDetails, budgetId, fromDate, toDate);
		return ResponseEntity.ok(expenses);
	}

	// ✅ List current month expenses by budget
	@GetMapping("/budget/{budgetId}/current-month")
	public ResponseEntity<List<ExpenseResponse>> listCurrentMonthByBudget(
			@AuthenticationPrincipal UserDetails userDetails,
			@PathVariable Long budgetId
	) {
		List<ExpenseResponse> expenses = expenseService.listExpenseCurrentMonthByBudget(userDetails, budgetId);
		return ResponseEntity.ok(expenses);
	}

	// ✅ Update expense
	@PutMapping("/{expenseId}")
	public ResponseEntity<ExpenseResponse> updateExpense(
			@AuthenticationPrincipal UserDetails userDetails,
			@PathVariable Long expenseId,
			@Valid @RequestBody ExpenseRequest request
	) throws Exception {
		ExpenseResponse updatedExpense = expenseService.updateExpense(userDetails, expenseId, request);
		return ResponseEntity.ok(updatedExpense);
	}

	// ✅ Delete expense
	@DeleteMapping("/{expenseId}")
	public ResponseEntity<Void> deleteExpense(
			@AuthenticationPrincipal UserDetails userDetails,
			@PathVariable Long expenseId
	) {
		expenseService.deleteExpense(userDetails, expenseId);
		return ResponseEntity.noContent().build();
	}

	// ✅ NEW: Unified filter endpoint (Category + Subcategory + PaymentType + DateRange)
	@GetMapping("/filter")
	public ResponseEntity<List<ExpenseResponse>> filterExpenses(
			@AuthenticationPrincipal UserDetails userDetails,
			@RequestParam(required = false) Long budgetId,
			@RequestParam(required = false) Long subCategoryId,
			@RequestParam(required = false) String paymentType,
			@RequestParam(required = false) String startDate,
			@RequestParam(required = false) String endDate
	) {
		List<ExpenseResponse> filteredExpenses = expenseService.filterExpenses(
				userDetails, budgetId, subCategoryId, paymentType, startDate, endDate
		);
		return ResponseEntity.ok(filteredExpenses);
	}
}
