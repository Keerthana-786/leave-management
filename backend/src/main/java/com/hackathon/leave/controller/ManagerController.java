package com.hackathon.leave.controller;

import com.hackathon.leave.dto.LeaveResponseDto;
import com.hackathon.leave.dto.PageResponseDto;
import com.hackathon.leave.model.LeaveStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/manager")
@Tag(name = "Manager", description = "Endpoints for managers to view and review team leave requests")
public class ManagerController {

    @GetMapping("/requests")
    @PreAuthorize("hasAnyRole('MANAGER', 'HR')")
    @Operation(summary = "Get manager approval requests", description = "Retrieves team leave requests assigned to the manager with filtering and pagination")
    public ResponseEntity<PageResponseDto<LeaveResponseDto>> getManagerRequests(
            @Parameter(description = "Filter by leave status") @RequestParam(required = false) LeaveStatus status,
            @Parameter(description = "Filter by leave type code") @RequestParam(required = false) String type,
            @Parameter(description = "Filter leaves starting from date") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(description = "Filter leaves ending up to date") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @Parameter(description = "Search query by employee name or reason") @RequestParam(required = false) String q,
            @Parameter(description = "Sort expression e.g. createdAt,desc") @RequestParam(defaultValue = "createdAt,desc") String sort,
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }
}
