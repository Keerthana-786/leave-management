package com.hackathon.leave.controller;

import com.hackathon.leave.dto.*;
import com.hackathon.leave.model.User;
import com.hackathon.leave.security.SecurityUtils;
import com.hackathon.leave.service.LeaveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leaves")
@Tag(name = "Leaves", description = "Endpoints for applying, previewing, managing, and tracking leave requests")
public class LeaveController {

    private final LeaveService leaveService;
    private final SecurityUtils securityUtils;

    public LeaveController(LeaveService leaveService, SecurityUtils securityUtils) {
        this.leaveService = leaveService;
        this.securityUtils = securityUtils;
    }

    @PostMapping("/preview")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Preview leave request", description = "Calculates working days, checks balances, and evaluates team conflict threshold before submission")
    public ResponseEntity<LeavePreviewResponse> previewLeave(@Valid @RequestBody LeavePreviewRequest request) {
        User currentUser = securityUtils.getCurrentUser();
        LeavePreviewResponse response = leaveService.preview(currentUser, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Submit leave request", description = "Applies for a new leave, reserves pending balance, and initializes the approval chain")
    public ResponseEntity<LeaveResponseDto> applyLeave(@Valid @RequestBody LeaveApplyRequest request) {
        User currentUser = securityUtils.getCurrentUser();
        LeaveResponseDto response = leaveService.apply(currentUser, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/mine")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get user leave requests", description = "Retrieves all leave requests filed by the currently logged-in user")
    public ResponseEntity<List<LeaveResponseDto>> getMyLeaves() {
        User currentUser = securityUtils.getCurrentUser();
        List<LeaveResponseDto> leaves = leaveService.getMyLeaves(currentUser);
        return ResponseEntity.ok(leaves);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get leave request by ID", description = "Retrieves a specific leave request's details")
    public ResponseEntity<LeaveResponseDto> getLeaveById(@PathVariable Long id) {
        User currentUser = securityUtils.getCurrentUser();
        LeaveResponseDto leave = leaveService.getLeaveById(currentUser, id);
        return ResponseEntity.ok(leave);
    }

    @GetMapping("/{id}/timeline")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get leave request timeline", description = "Fetches the full timeline including approval steps and audit trail for a leave request")
    public ResponseEntity<LeaveTimelineDto> getLeaveTimeline(@PathVariable Long id) {
        User currentUser = securityUtils.getCurrentUser();
        LeaveTimelineDto timeline = leaveService.getLeaveTimeline(currentUser, id);
        return ResponseEntity.ok(timeline);
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Cancel leave request", description = "Cancels an existing leave request and restores pending or used leave balance")
    public ResponseEntity<LeaveResponseDto> cancelLeave(@PathVariable Long id) {
        User currentUser = securityUtils.getCurrentUser();
        LeaveResponseDto cancelled = leaveService.cancelLeave(currentUser, id);
        return ResponseEntity.ok(cancelled);
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('MANAGER', 'HR')")
    @Operation(summary = "Approve leave request step", description = "Records approval for current step and advances the workflow or completes final approval")
    public ResponseEntity<LeaveResponseDto> approveLeave(@PathVariable Long id, @RequestBody(required = false) ApprovalDecisionRequest request) {
        User currentUser = securityUtils.getCurrentUser();
        LeaveResponseDto approved = leaveService.approveLeave(currentUser, id, request);
        return ResponseEntity.ok(approved);
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('MANAGER', 'HR')")
    @Operation(summary = "Reject leave request step", description = "Rejects the leave request, records reason/comment, and releases pending balance")
    public ResponseEntity<LeaveResponseDto> rejectLeave(@PathVariable Long id, @RequestBody(required = false) ApprovalDecisionRequest request) {
        User currentUser = securityUtils.getCurrentUser();
        LeaveResponseDto rejected = leaveService.rejectLeave(currentUser, id, request);
        return ResponseEntity.ok(rejected);
    }
}
