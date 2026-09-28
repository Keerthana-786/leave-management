package com.hackathon.leave.controller;

import com.hackathon.leave.dto.DemoActionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/demo")
@Tag(name = "Demo", description = "Endpoints for fast interactive demo evaluation (escalation timeout simulation and scenario reseeding)")
public class DemoController {

    @PostMapping("/simulate-timeout/{id}")
    @PreAuthorize("hasRole('HR')")
    @Operation(summary = "Simulate escalation timeout", description = "Forces an active PENDING_MANAGER leave request past its SLA to trigger automatic escalation to HR")
    public ResponseEntity<DemoActionResponse> simulateTimeout(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @PostMapping("/reset-seed")
    @PreAuthorize("hasRole('HR')")
    @Operation(summary = "Reset database to initial seed scenarios", description = "Wipes and reseeds users, teams, leave types, balances, requests, and audit logs to the pristine demo state")
    public ResponseEntity<DemoActionResponse> resetSeed() {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }
}
