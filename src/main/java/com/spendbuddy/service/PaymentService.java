package com.spendbuddy.service;

import com.spendbuddy.entity.auth.User;
import com.spendbuddy.entity.expensetracker.PaymentType;
import com.spendbuddy.exception.handler.EntityException;
import com.spendbuddy.repository.PaymentTypeRepository;
import com.spendbuddy.repository.UserRepository;
import com.spendbuddy.request.dto.PaymentTypeRequest;
import com.spendbuddy.request.dto.UpdatePaymentTypeRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PaymentService {

	@Autowired
	private PaymentTypeRepository repository;

	@Autowired
	private UserRepository userRepository;

	//  Fetch all payment types for the currently logged-in user
	public List<PaymentType> list() {
		User user = getCurrentUser();
		return repository.findByUser(user);
	}

	//  Save a new payment type and assign it to the logged-in user
	public PaymentType save(PaymentTypeRequest request) {
		User user = getCurrentUser();
		PaymentType paymentType = new PaymentType();
		paymentType.setType(request.getType());
		paymentType.setActive(true);
		paymentType.setUser(user);
		return repository.save(paymentType);
	}

	//  Update only if the payment type belongs to the current user
	public PaymentType update(Long paymentId, UpdatePaymentTypeRequest request) throws Exception {
		User user = getCurrentUser();
		PaymentType type = repository.findByIdAndUser(paymentId, user)
				.orElseThrow(() -> new EntityException("Payment Type not found for current user"));
		type.setType(request.getType());
		type.setActive(request.isActive());
		return repository.save(type);
	}

	//  Helper to get the logged-in user from JWT/SecurityContext
	private User getCurrentUser() {
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		String username;
		if (principal instanceof UserDetails) {
			username = ((UserDetails) principal).getUsername();
		} else {
			username = principal.toString();
		}
		return userRepository.findByUsername(username)
				.orElseThrow(() -> new EntityException("User not found"));
	}
}
