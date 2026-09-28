package com.hackathon.leave.controller;

import com.hackathon.leave.dto.LeaveResponseDto;
import com.hackathon.leave.dto.PageResponseDto;
import com.hackathon.leave.model.LeaveStatus;
import com.hackathon.leave.service.ManagerHrService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/hr")
@Tag(name = "HR", description = "Endpoints for HR administrators to oversee organization-wide leaves and escalations")
public class HrController {

    private final ManagerHrService managerHrService;

    public HrController(ManagerHrService managerHrService) {
        this.managerHrService = managerHrService;
    }

    @GetMapping("/requests")
    @PreAuthorize("hasRole('HR')")
    @Operation(summary = "Get HR leave requests", description = "Retrieves organization-wide leave requests, escalations, and pending HR reviews with filtering")
    public ResponseEntity<PageResponseDto<LeaveResponseDto>> getHrRequests(
            @Parameter(description = "Filter by leave status") @RequestParam(required = false) LeaveStatus status,
            @Parameter(description = "Filter by leave type code") @RequestParam(required = false) String type,
            @Parameter(description = "Filter leaves starting from date") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(description = "Filter leaves ending up to date") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @Parameter(description = "Search query by employee name or reason") @RequestParam(required = false) String q,
            @Parameter(description = "Sort expression e.g. createdAt,desc") @RequestParam(defaultValue = "createdAt,desc") String sort,
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size
    ) {
        PageResponseDto<LeaveResponseDto> result = managerHrService.getHrRequests(
                status, type, from, to, q, sort, page, size
        );
        return ResponseEntity.ok(result);
    }
}
