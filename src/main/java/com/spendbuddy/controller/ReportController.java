package com.spendbuddy.controller;

import com.spendbuddy.response.dto.ReportResponse;
import com.spendbuddy.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ReportController {

    @Autowired
    private ReportService reportService;

    @GetMapping("/expense-by-category")
    public List<ReportResponse> expenseByCategory(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(value = "range", required = false, defaultValue = "6m") String range) {

        boolean lastSixMonths = !"all".equalsIgnoreCase(range);
        return reportService.getExpenseByCategory(userDetails, lastSixMonths);
    }

    @GetMapping("/monthly-summary")
    public List<ReportResponse> monthlySummary(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(value = "range", required = false, defaultValue = "6m") String range) {

        boolean lastSixMonths = !"all".equalsIgnoreCase(range);
        return reportService.getMonthlySummary(userDetails, lastSixMonths);
    }

    @GetMapping("/payment-type-summary")
    public List<ReportResponse> paymentTypeSummary(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(value = "range", required = false, defaultValue = "6m") String range) {

        boolean lastSixMonths = !"all".equalsIgnoreCase(range);
        return reportService.getPaymentTypeSummary(userDetails, lastSixMonths);
    }
}
