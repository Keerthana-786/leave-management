package com.hackathon.leave.controller;

import com.hackathon.leave.dto.LeaveBalanceDto;
import com.hackathon.leave.model.User;
import com.hackathon.leave.security.SecurityUtils;
import com.hackathon.leave.service.BalanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/balances")
@Tag(name = "Balances", description = "Endpoints for checking leave entitlements, used, pending, and remaining balances")
public class BalanceController {

    private final BalanceService balanceService;
    private final SecurityUtils securityUtils;

    public BalanceController(BalanceService balanceService, SecurityUtils securityUtils) {
        this.balanceService = balanceService;
        this.securityUtils = securityUtils;
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get user leave balances", description = "Retrieves entitled, used, pending, and remaining balances for the current user across all leave types")
    public ResponseEntity<List<LeaveBalanceDto>> getMyBalances() {
        User currentUser = securityUtils.getCurrentUser();
        List<LeaveBalanceDto> balances = balanceService.getMyBalances(currentUser);
        return ResponseEntity.ok(balances);
    }
}
