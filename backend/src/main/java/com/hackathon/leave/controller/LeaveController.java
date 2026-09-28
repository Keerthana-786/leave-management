package com.hackathon.leave.controller;

import com.hackathon.leave.dto.ApprovalDecisionRequest;
import com.hackathon.leave.dto.LeaveApplyRequest;
import com.hackathon.leave.dto.LeavePreviewRequest;
import com.hackathon.leave.dto.LeavePreviewResponse;
import com.hackathon.leave.dto.LeaveResponseDto;
import com.hackathon.leave.dto.LeaveTimelineDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/leaves")
@Tag(name = "Leaves", description = "Endpoints for applying, previewing, managing, and tracking leave requests")
public class LeaveController {

    @PostMapping("/preview")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Preview leave request", description = "Calculates working days, checks balances, and evaluates team conflict threshold before submission")
    public ResponseEntity<LeavePreviewResponse> previewLeave(@Valid @RequestBody LeavePreviewRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Submit leave request", description = "Applies for a new leave, reserves pending balance, and initializes the approval chain")
    public ResponseEntity<LeaveResponseDto> applyLeave(@Valid @RequestBody LeaveApplyRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @GetMapping("/mine")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get user leave requests", description = "Retrieves all leave requests filed by the currently logged-in user")
    public ResponseEntity<List<LeaveResponseDto>> getMyLeaves() {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get leave request by ID", description = "Retrieves a specific leave request's details")
    public ResponseEntity<LeaveResponseDto> getLeaveById(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @GetMapping("/{id}/timeline")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get leave request timeline", description = "Fetches the full timeline including approval steps and audit trail for a leave request")
    public ResponseEntity<LeaveTimelineDto> getLeaveTimeline(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Cancel leave request", description = "Cancels an existing leave request and restores pending or used leave balance")
    public ResponseEntity<LeaveResponseDto> cancelLeave(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('MANAGER', 'HR')")
    @Operation(summary = "Approve leave request step", description = "Records approval for current step and advances the workflow or completes final approval")
    public ResponseEntity<LeaveResponseDto> approveLeave(@PathVariable Long id, @RequestBody(required = false) ApprovalDecisionRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('MANAGER', 'HR')")
    @Operation(summary = "Reject leave request step", description = "Rejects the leave request, records reason/comment, and releases pending balance")
    public ResponseEntity<LeaveResponseDto> rejectLeave(@PathVariable Long id, @RequestBody(required = false) ApprovalDecisionRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }
}
