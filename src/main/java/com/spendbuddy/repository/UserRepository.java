package com.spendbuddy.repository;

import java.util.Optional;

import com.spendbuddy.entity.auth.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByUsername(String username);
	Optional<User> findByEmail(String email); // ✅ added for email-based login

	Boolean existsByUsername(String username);
	Boolean existsByEmail(String email);
}
