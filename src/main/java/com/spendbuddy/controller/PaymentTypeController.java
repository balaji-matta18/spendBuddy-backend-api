package com.spendbuddy.controller;

import java.util.List;

import com.spendbuddy.entity.expensetracker.PaymentType;
import com.spendbuddy.request.dto.PaymentTypeRequest;
import com.spendbuddy.request.dto.UpdatePaymentTypeRequest;
import com.spendbuddy.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@Validated
@RequestMapping("/api/paymenttype")
public class PaymentTypeController {

	@Autowired
	private PaymentService service;

	//  Fetch payment types belonging to the logged-in user only
	@GetMapping
	public List<PaymentType> list() {
		return service.list();
	}

	//  Create a new payment type for the logged-in user
	@PostMapping
	public PaymentType save(@Valid @RequestBody PaymentTypeRequest request) {
		return service.save(request);
	}

	//  Update a payment type (only if it belongs to current user)
	@PutMapping("/{paymentId}")
	public PaymentType update(
			@Valid @RequestBody UpdatePaymentTypeRequest request,
			@PathVariable Long paymentId) throws Exception {
		return service.update(paymentId, request);
	}
}
