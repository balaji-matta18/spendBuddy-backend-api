package com.spendbuddy.service;

import com.spendbuddy.entity.auth.User;
import com.spendbuddy.entity.budget.Budget;
import com.spendbuddy.entity.expensetracker.SubCategory;
import com.spendbuddy.exception.handler.EntityException;
import com.spendbuddy.repository.BudgetRepository;
import com.spendbuddy.repository.SubCategoryRepository;
import com.spendbuddy.repository.UserRepository;
import com.spendbuddy.request.dto.SubCategoryRequest;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SubCategoryService {

	@Autowired
	private SubCategoryRepository subCategoryRepository;

	@Autowired
	private BudgetRepository budgetRepository;

	@Autowired
	private UserRepository userRepository;

	/**
	 * ✅ Create a new subcategory under a budget
	 */
	@Transactional
	public SubCategory save(UserDetails userDetail, SubCategoryRequest subCategoryRequest) {
		User user = userRepository.findByUsername(userDetail.getUsername())
				.orElseThrow(() -> new UsernameNotFoundException(
						"User not found with username: " + userDetail.getUsername()
				));

		// ✅ Validate Budget
		Budget budget = budgetRepository.findById(subCategoryRequest.getBudgetId())
				.orElseThrow(() -> new EntityException(
						"Budget not found for id: " + subCategoryRequest.getBudgetId()
				));

		if (!budget.getUser().getId().equals(user.getId())) {
			throw new EntityException("Budget does not belong to the current user");
		}

		// ✅ Create SubCategory
		SubCategory subCategory = new SubCategory();
		subCategory.setName(subCategoryRequest.getName());
		subCategory.setBudget(budget);
		subCategory.setActive(true);

		return subCategoryRepository.save(subCategory);
	}

	/**
	 *  List all subcategories for a given budget
	 */
	@Transactional
	public List<SubCategory> listByBudget(UserDetails userDetails, Long budgetId) {
		User user = userRepository.findByUsername(userDetails.getUsername())
				.orElseThrow(() -> new UsernameNotFoundException(
						"User not found with username: " + userDetails.getUsername()
				));

		Budget budget = budgetRepository.findById(budgetId)
				.orElseThrow(() -> new EntityException("Budget not found for id: " + budgetId));

		if (!budget.getUser().getId().equals(user.getId())) {
			throw new EntityException("Budget does not belong to the current user");
		}

		return subCategoryRepository.listSubCategory(budgetId, user.getId());


	}
}
