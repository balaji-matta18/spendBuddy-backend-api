package com.spendbuddy.response.dto;

import com.spendbuddy.entity.expensetracker.Expense;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExpenseResponse {

	private Long id;
	private Double amount;
	private String description;
	private String category;
	private String subCategory;
	private String paymentType;
	private String expenseDate;

	public static ExpenseResponse fromEntity(Expense expense) {
		return ExpenseResponse.builder()
				.id(expense.getId())
				.amount(expense.getExpenseAmount())
				.description(expense.getExpenseDescription())
				.category(expense.getBudget() != null ? expense.getBudget().getCategory() : null)
				.subCategory(expense.getSubCategory() != null ? expense.getSubCategory().getName() : null)
				.paymentType(expense.getPayment() != null ? expense.getPayment().getType() : null)
				.expenseDate(expense.getExpenseDate() != null ? expense.getExpenseDate().toString() : null)
				.build();
	}
}
