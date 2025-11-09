package com.spendbuddy.service;

import com.spendbuddy.entity.auth.User;
import com.spendbuddy.entity.expensetracker.Category;
import com.spendbuddy.exception.handler.EntityException;
import com.spendbuddy.repository.CategoryRepository;
import com.spendbuddy.repository.UserRepository;
import com.spendbuddy.request.dto.CategoryRequest;
import com.spendbuddy.response.dto.CategoryResponse;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private CategoryRepository categoryRepository;

	// ✅ Save new category
	@Transactional
	public Category save(UserDetails userDetail, CategoryRequest categoryRequest) {
		User user = userRepository.findByUsername(userDetail.getUsername())
				.orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + userDetail.getUsername()));

		// Prevent duplicate category names for same user
		boolean exists = categoryRepository.existsByNameAndUserId(categoryRequest.getName(), user.getId());
		if (exists) {
			throw new EntityException("Category with name '" + categoryRequest.getName() + "' already exists for this user");
		}

		Category category = new Category();
		category.setName(categoryRequest.getName());
		category.setActive(true);
		category.setUser(user);

		return categoryRepository.save(category);
	}

	// ✅ List all categories for logged-in user
	@Transactional
	public List<CategoryResponse> list(UserDetails userDetail) {
		User user = userRepository.findByUsername(userDetail.getUsername())
				.orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + userDetail.getUsername()));

		// ✅ user.getId() is String now — repository will handle String userId
		return categoryRepository.listCategory(user.getId());
	}
}
