package com.hackathon.leave.controller;

import com.hackathon.leave.config.DataLoader;
import com.hackathon.leave.dto.DemoActionResponse;
import com.hackathon.leave.dto.UserSummaryDto;
import com.hackathon.leave.exception.ConflictException;
import com.hackathon.leave.exception.ResourceNotFoundException;
import com.hackathon.leave.model.ApprovalDecision;
import com.hackathon.leave.model.ApprovalStage;
import com.hackathon.leave.model.ApprovalStep;
import com.hackathon.leave.model.LeaveRequest;
import com.hackathon.leave.repository.ApprovalStepRepository;
import com.hackathon.leave.repository.LeaveRequestRepository;
import com.hackathon.leave.repository.UserRepository;
import com.hackathon.leave.service.EscalationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/demo")
@Tag(name = "Demo", description = "Endpoints for fast interactive demo evaluation (escalation timeout simulation and scenario reseeding)")
public class DemoController {

    private final UserRepository userRepository;
    private final DataLoader dataLoader;
    private final ApprovalStepRepository approvalStepRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final EscalationService escalationService;

    public DemoController(
            UserRepository userRepository,
            DataLoader dataLoader,
            ApprovalStepRepository approvalStepRepository,
            LeaveRequestRepository leaveRequestRepository,
            EscalationService escalationService
    ) {
        this.userRepository = userRepository;
        this.dataLoader = dataLoader;
        this.approvalStepRepository = approvalStepRepository;
        this.leaveRequestRepository = leaveRequestRepository;
        this.escalationService = escalationService;
    }

    @GetMapping("/users")
    @Operation(summary = "Get demo users list", description = "Public endpoint listing all users for one-click persona login in demo mode")
    public ResponseEntity<List<UserSummaryDto>> getDemoUsers() {
        List<UserSummaryDto> users = userRepository.findAll().stream()
                .map(AuthController::toSummaryDto)
                .toList();
        return ResponseEntity.ok(users);
    }

    @PostMapping("/simulate-timeout/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Simulate escalation timeout", description = "Forces an active PENDING_MANAGER leave request past its SLA to trigger automatic escalation to HR")
    public ResponseEntity<DemoActionResponse> simulateTimeout(@PathVariable Long id) {
        LeaveRequest request = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found: " + id));

        List<ApprovalStep> steps = approvalStepRepository.findByRequestIdOrderByIdAsc(id);
        ApprovalStep targetStep = null;
        for (ApprovalStep s : steps) {
            if (s.getDecision() == ApprovalDecision.PENDING && s.getStage() == ApprovalStage.MANAGER) {
                targetStep = s;
                break;
            }
        }

        if (targetStep == null) {
            throw new ConflictException("No pending manager step found for leave request #" + id);
        }

        targetStep.setDueAt(LocalDateTime.now().minusMinutes(5));
        approvalStepRepository.save(targetStep);

        escalationService.escalateStale();

        return ResponseEntity.ok(new DemoActionResponse(true, "Request #" + id + " SLA timed out and escalated to HR"));
    }

    @PostMapping("/reset-seed")
    @PreAuthorize("hasRole('HR')")
    @Operation(summary = "Reset database to initial seed scenarios", description = "Wipes and reseeds users, teams, leave types, balances, requests, and audit logs to the pristine demo state")
    public ResponseEntity<DemoActionResponse> resetSeed() {
        dataLoader.resetData();
        return ResponseEntity.ok(new DemoActionResponse(true, "Database successfully reset to initial demo seed scenarios"));
    }
}
