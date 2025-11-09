package com.spendbuddy.service;

import com.spendbuddy.entity.auth.User;
import com.spendbuddy.entity.budget.Budget;
import com.spendbuddy.entity.expensetracker.Expense;
import com.spendbuddy.entity.expensetracker.PaymentType;
import com.spendbuddy.entity.expensetracker.SubCategory;
import com.spendbuddy.exception.handler.EntityException;
import com.spendbuddy.repository.*;
import com.spendbuddy.request.dto.ExpenseRequest;
import com.spendbuddy.response.dto.ExpenseResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExpenseService {

	@Autowired
	private ExpenseRepository expenseRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private BudgetRepository budgetRepository;

	@Autowired
	private SubCategoryRepository subCategoryRepository;

	@Autowired
	private PaymentTypeRepository paymentTypeRepository;

	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	// ✅ Create new expense under a budget
	@Transactional
	public ExpenseResponse save(UserDetails userDetails, ExpenseRequest request) throws Exception {
		User user = getUser(userDetails);
		Budget budget = budgetRepository.findById(request.getBudgetId())
				.orElseThrow(() -> new EntityException("Budget not found with id: " + request.getBudgetId()));

		if (!budget.getUser().getId().equals(user.getId())) {
			throw new EntityException("Budget does not belong to current user");
		}

		Expense expense = new Expense();
		expense.setUser(user);
		expense.setBudget(budget);
		expense.setExpenseAmount(request.getAmount());
		expense.setExpenseDescription(request.getExpenseDescription());

		// ✅ Parse expense date safely
		if (request.getExpenseDate() != null) {
			try {
				expense.setExpenseDate(LocalDate.parse(request.getExpenseDate(), FORMATTER));
			} catch (Exception e) {
				throw new EntityException("Invalid expenseDate format. Expected yyyy-MM-dd");
			}
		} else {
			throw new EntityException("Expense date is required");
		}

		// ✅ Fetch payment type by ID
		if (request.getPaymentTypeId() != null) {
			PaymentType paymentType = paymentTypeRepository.findById(request.getPaymentTypeId())
					.orElseThrow(() -> new EntityException("Payment type not found with id: " + request.getPaymentTypeId()));
			expense.setPayment(paymentType);
		} else {
			throw new EntityException("Payment type is required");
		}

		// ✅ Optional subcategory
		if (request.getSubCategoryId() != null) {
			SubCategory subCategory = subCategoryRepository.findById(request.getSubCategoryId())
					.orElseThrow(() -> new EntityException("SubCategory not found for id: " + request.getSubCategoryId()));
			expense.setSubCategory(subCategory);
		}

		expense.setCreatedAt(LocalDateTime.now());
		expense.setUpdatedAt(LocalDateTime.now());

		Expense saved = expenseRepository.save(expense);
		return ExpenseResponse.fromEntity(saved);
	}

	// ✅ List all or by date range
	@Transactional(readOnly = true)
	public List<ExpenseResponse> list(UserDetails userDetails, String fromDate, String toDate) {
		User user = getUser(userDetails);
		List<Expense> expenses;

		if (fromDate != null && toDate != null) {
			LocalDate from = LocalDate.parse(fromDate, FORMATTER);
			LocalDate to = LocalDate.parse(toDate, FORMATTER);
			expenses = expenseRepository.findByUserAndExpenseDateBetween(user, from, to);
		} else {
			expenses = expenseRepository.findByUser(user);
		}

		return expenses.stream()
				.map(ExpenseResponse::fromEntity)
				.collect(Collectors.toList());
	}

	// ✅ List current month expenses
	@Transactional(readOnly = true)
	public List<ExpenseResponse> listExpenseCurrentMonth(UserDetails userDetails) {
		User user = getUser(userDetails);
		LocalDate start = LocalDate.now().withDayOfMonth(1);
		LocalDate end = LocalDate.now();
		List<Expense> expenses = expenseRepository.findByUserAndExpenseDateBetween(user, start, end);
		return expenses.stream()
				.map(ExpenseResponse::fromEntity)
				.collect(Collectors.toList());
	}

	// ✅ List expenses by budget
	@Transactional(readOnly = true)
	public List<ExpenseResponse> listExpenseByBudget(UserDetails userDetails, Long budgetId, String fromDate, String toDate) {
		User user = getUser(userDetails);
		Budget budget = getBudget(budgetId, user);

		List<Expense> expenses;
		if (fromDate != null && toDate != null) {
			LocalDate from = LocalDate.parse(fromDate, FORMATTER);
			LocalDate to = LocalDate.parse(toDate, FORMATTER);
			expenses = expenseRepository.findByBudgetAndUserAndExpenseDateBetween(budget, user, from, to);
		} else {
			expenses = expenseRepository.findByBudgetAndUser(budget, user);
		}

		return expenses.stream()
				.map(ExpenseResponse::fromEntity)
				.collect(Collectors.toList());
	}

	// ✅ List current month expenses by budget
	@Transactional(readOnly = true)
	public List<ExpenseResponse> listExpenseCurrentMonthByBudget(UserDetails userDetails, Long budgetId) {
		User user = getUser(userDetails);
		Budget budget = getBudget(budgetId, user);
		LocalDate start = LocalDate.now().withDayOfMonth(1);
		LocalDate end = LocalDate.now();
		List<Expense> expenses = expenseRepository.findByBudgetAndUserAndExpenseDateBetween(budget, user, start, end);
		return expenses.stream()
				.map(ExpenseResponse::fromEntity)
				.collect(Collectors.toList());
	}

	// ✅ Update existing expense
	@Transactional
	public ExpenseResponse updateExpense(UserDetails userDetails, Long expenseId, ExpenseRequest request) throws Exception {
		User user = getUser(userDetails);
		Expense expense = expenseRepository.findById(expenseId)
				.orElseThrow(() -> new EntityException("Expense not found"));

		if (!expense.getUser().getId().equals(user.getId())) {
			throw new EntityException("Expense does not belong to current user");
		}

		if (request.getAmount() != null) expense.setExpenseAmount(request.getAmount());
		if (request.getExpenseDescription() != null) expense.setExpenseDescription(request.getExpenseDescription());

		if (request.getExpenseDate() != null) {
			try {
				expense.setExpenseDate(LocalDate.parse(request.getExpenseDate(), FORMATTER));
			} catch (Exception e) {
				throw new EntityException("Invalid expenseDate format. Expected yyyy-MM-dd");
			}
		}

		if (request.getPaymentTypeId() != null) {
			PaymentType paymentType = paymentTypeRepository.findById(request.getPaymentTypeId())
					.orElseThrow(() -> new EntityException("Payment type not found for id: " + request.getPaymentTypeId()));
			expense.setPayment(paymentType);
		}

		expense.setUpdatedAt(LocalDateTime.now());
		Expense updated = expenseRepository.save(expense);
		return ExpenseResponse.fromEntity(updated);
	}

	// ✅ Delete expense
	@Transactional
	public void deleteExpense(UserDetails userDetails, Long expenseId) {
		User user = getUser(userDetails);
		Expense expense = expenseRepository.findById(expenseId)
				.orElseThrow(() -> new EntityException("Expense not found"));
		if (!expense.getUser().getId().equals(user.getId())) {
			throw new EntityException("Expense does not belong to current user");
		}
		expenseRepository.delete(expense);
	}

	// ✅ Helper for BudgetService.summary()
	@Transactional(readOnly = true)
	public double getSpentForBudget(UserDetails userDetails, Budget budget) {
		User user = getUser(userDetails);
		List<Expense> expenses = expenseRepository.findByBudgetAndUser(budget, user);
		return expenses.stream()
				.mapToDouble(Expense::getExpenseAmount)
				.sum();
	}

	// ✅ NEW: Flexible filter for Category + SubCategory + PaymentType + Date Range
	@Transactional(readOnly = true)
	public List<ExpenseResponse> filterExpenses(UserDetails userDetails,
												Long budgetId,
												Long subCategoryId,
												String paymentType,
												String startDate,
												String endDate) {

		User user = getUser(userDetails);
		List<Expense> expenses = expenseRepository.findByUser(user);

		if (budgetId != null) {
			expenses = expenses.stream()
					.filter(e -> e.getBudget() != null && e.getBudget().getId().equals(budgetId))
					.collect(Collectors.toList());
		}

		if (subCategoryId != null) {
			expenses = expenses.stream()
					.filter(e -> e.getSubCategory() != null && e.getSubCategory().getId().equals(subCategoryId))
					.collect(Collectors.toList());
		}

		if (paymentType != null && !paymentType.isEmpty()) {
			expenses = expenses.stream()
					.filter(e -> e.getPayment() != null &&
							e.getPayment().getType().equalsIgnoreCase(paymentType))
					.collect(Collectors.toList());
		}

		if (startDate != null && endDate != null) {
			LocalDate start = LocalDate.parse(startDate, FORMATTER);
			LocalDate end = LocalDate.parse(endDate, FORMATTER);
			expenses = expenses.stream()
					.filter(e -> !e.getExpenseDate().isBefore(start) && !e.getExpenseDate().isAfter(end))
					.collect(Collectors.toList());
		}

		return expenses.stream()
				.map(ExpenseResponse::fromEntity)
				.collect(Collectors.toList());
	}

	// Helpers
	private User getUser(UserDetails userDetails) {
		return userRepository.findByUsername(userDetails.getUsername())
				.orElseThrow(() -> new UsernameNotFoundException("User not found: " + userDetails.getUsername()));
	}

	private Budget getBudget(Long budgetId, User user) {
		Budget budget = budgetRepository.findById(budgetId)
				.orElseThrow(() -> new EntityException("Budget not found for id: " + budgetId));
		if (!budget.getUser().getId().equals(user.getId())) {
			throw new EntityException("Budget does not belong to current user");
		}
		return budget;
	}
}
