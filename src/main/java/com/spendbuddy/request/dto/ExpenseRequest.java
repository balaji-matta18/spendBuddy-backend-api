package com.spendbuddy.request.dto;

import jakarta.validation.constraints.NotNull;

public class ExpenseRequest {

	@NotNull(message = "Expense amount is required")
	private Double amount;

	@NotNull(message = "Expense description is required")
	private String expenseDescription;

	@NotNull(message = "Budget ID is required")
	private Long budgetId;

	private Long subCategoryId;

	@NotNull(message = "Expense date is required")
	private String expenseDate;       // accept "yyyy-MM-dd"

	private Long paymentTypeId;       // id of PaymentType

	// getters and setters
	public Double getAmount() { return amount; }
	public void setAmount(Double amount) { this.amount = amount; }
	public String getExpenseDescription() { return expenseDescription; }
	public void setExpenseDescription(String expenseDescription) { this.expenseDescription = expenseDescription; }
	public Long getBudgetId() { return budgetId; }
	public void setBudgetId(Long budgetId) { this.budgetId = budgetId; }
	public Long getSubCategoryId() { return subCategoryId; }
	public void setSubCategoryId(Long subCategoryId) { this.subCategoryId = subCategoryId; }
	public String getExpenseDate() { return expenseDate; }
	public void setExpenseDate(String expenseDate) { this.expenseDate = expenseDate; }
	public Long getPaymentTypeId() { return paymentTypeId; }
	public void setPaymentTypeId(Long paymentTypeId) { this.paymentTypeId = paymentTypeId; }
}
