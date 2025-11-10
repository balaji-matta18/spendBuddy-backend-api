package com.spendbuddy.repository;

import com.spendbuddy.entity.auth.User;
import com.spendbuddy.entity.expensetracker.PaymentType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentTypeRepository extends JpaRepository<PaymentType, Long> {

	boolean existsById(Long id);

	//  Fetch all payment types belonging to a specific user
	List<PaymentType> findByUser(User user);

	//  Fetch a single payment type by ID that belongs to a specific user
	Optional<PaymentType> findByIdAndUser(Long id, User user);
}
