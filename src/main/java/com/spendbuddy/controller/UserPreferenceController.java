package com.spendbuddy.controller;

import com.spendbuddy.entity.auth.User;
import com.spendbuddy.repository.UserRepository;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user/preferences")
public class UserPreferenceController {

    @Autowired
    private UserRepository userRepository;

    /**
     * ✅ Update the user's preferred month start day (1–28)
     */
    @PutMapping("/month-start-day")
    public ResponseEntity<?> updateMonthStartDay(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam @Min(1) @Max(28) Integer day
    ) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("User not found"));

        user.setMonthStartDay(day);
        userRepository.save(user);

        return ResponseEntity.ok("Month start day updated to " + day);
    }

    /**
     * ✅ Fetch current preference
     */
    @GetMapping("/month-start-day")
    public ResponseEntity<?> getMonthStartDay(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("User not found"));

        return ResponseEntity.ok(user.getMonthStartDay());
    }
}
