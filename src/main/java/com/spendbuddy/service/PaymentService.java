package com.spendbuddy.service;

import java.util.List;

import com.spendbuddy.entity.expensetracker.PaymentType;
import com.spendbuddy.exception.handler.EntityException;
import com.spendbuddy.repository.PaymentTypeRepository;
import com.spendbuddy.request.dto.PaymentTypeRequest;
import com.spendbuddy.request.dto.UpdatePaymentTypeRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {


	@Autowired
	private PaymentTypeRepository repository;

	public List<PaymentType> list(){
		return repository.findAll();
	}

	public PaymentType save(PaymentTypeRequest request) {
		PaymentType paymentType=new PaymentType();
		paymentType.setType(request.getType());
		paymentType.setActive(true);
		return repository.save(paymentType);

	}

	public PaymentType update(Long paymentId, UpdatePaymentTypeRequest request) throws Exception {
		PaymentType type=repository.findById(paymentId).orElseThrow(() -> new EntityException("Payment Type not found"));
		type.setType(request.getType());
		type.setActive(request.isActive());
		return repository.save(type);
	}
}
