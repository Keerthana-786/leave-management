package com.hackathon.leave.controller;

import com.hackathon.leave.dto.AnalyticsSummaryDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
@Tag(name = "Analytics", description = "Endpoints for leadership and HR to inspect organization and team leave statistics")
public class AnalyticsController {

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('MANAGER', 'HR')")
    @Operation(summary = "Get leave analytics summary", description = "Aggregates overall leave metrics, rejection rates, escalation counts, and leave type distribution")
    public ResponseEntity<AnalyticsSummaryDto> getAnalyticsSummary() {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }
}
