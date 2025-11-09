package com.spendbuddy.controller;

import com.spendbuddy.entity.expensetracker.SubCategory;
import com.spendbuddy.request.dto.SubCategoryRequest;
import com.spendbuddy.service.SubCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@Validated
@RequestMapping("/api/subcategory")
public class SubCategoryController {

	@Autowired
	private SubCategoryService subCategoryService;

	/**
	 * ✅ List all subcategories under a given budget
	 */
	@GetMapping
	public List<SubCategory> listByBudget(@RequestParam(required = true, name = "budgetId") Long budgetId) {
		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext()
				.getAuthentication().getPrincipal();

		return subCategoryService.listByBudget(userDetails, budgetId);
	}

	/**
	 * ✅ Create a new subcategory under a budget
	 */
	@PostMapping
	public ResponseEntity<SubCategory> createSubCategory(@Validated @RequestBody SubCategoryRequest subCategoryRequest) {
		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext()
				.getAuthentication().getPrincipal();

		return ResponseEntity.ok(subCategoryService.save(userDetails, subCategoryRequest));
	}
}
