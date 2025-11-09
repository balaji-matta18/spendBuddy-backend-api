package com.spendbuddy.request.dto;

import lombok.Data;

@Data
public class SubCategoryRequest {

	private String name;

	// ✅ Added after replacing Category → Budget
	private Long budgetId;
}
