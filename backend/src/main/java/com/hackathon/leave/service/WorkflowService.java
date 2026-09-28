package com.hackathon.leave.service;

import com.hackathon.leave.dto.LeaveResponseDto;
import com.hackathon.leave.exception.ConflictException;
import com.hackathon.leave.exception.ResourceNotFoundException;
import com.hackathon.leave.model.*;
import com.hackathon.leave.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class WorkflowService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final ApprovalStepRepository approvalStepRepository;
    private final AuditEventRepository auditEventRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final DelegationService delegationService;
    private final int escalationTimeoutMinutes;

    public WorkflowService(
            LeaveRequestRepository leaveRequestRepository,
            LeaveBalanceRepository leaveBalanceRepository,
            ApprovalStepRepository approvalStepRepository,
            AuditEventRepository auditEventRepository,
            UserRepository userRepository,
            NotificationService notificationService,
            DelegationService delegationService,
            @Value("${leave.escalation-timeout-minutes:1}") int escalationTimeoutMinutes
    ) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.leaveBalanceRepository = leaveBalanceRepository;
        this.approvalStepRepository = approvalStepRepository;
        this.auditEventRepository = auditEventRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.delegationService = delegationService;
        this.escalationTimeoutMinutes = escalationTimeoutMinutes;
    }

    public enum Action {
        APPROVE,
        REJECT,
        CANCEL,
        ESCALATE
    }

    @Transactional
    public LeaveResponseDto transition(Long requestId, Action action, User actor, String comment) {
        LeaveRequest request = leaveRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found with id: " + requestId));

        LeaveStatus currentStatus = request.getStatus();

        // 1. Final state guard
        if (currentStatus == LeaveStatus.APPROVED || currentStatus == LeaveStatus.REJECTED || currentStatus == LeaveStatus.CANCELLED) {
            throw new ConflictException("Cannot " + action + " from " + currentStatus);
        }

        // 2. Prevent self-decision (except CANCEL which is owner-only)
        if (action != Action.CANCEL && action != Action.ESCALATE && actor != null) {
            if (request.getEmployee().getId().equals(actor.getId())) {
                throw new AccessDeniedException("You cannot decide your own leave request");
            }
        }

        switch (action) {
            case CANCEL -> handleCancel(request, actor, comment);
            case REJECT -> handleReject(request, actor, comment);
            case APPROVE -> handleApprove(request, actor, comment);
            case ESCALATE -> handleEscalate(request, comment);
        }

        request.setLastActionAt(LocalDateTime.now());
        LeaveRequest saved = leaveRequestRepository.save(request);
        return toDto(saved);
    }

    private void handleCancel(LeaveRequest request, User actor, String comment) {
        if (actor != null && !request.getEmployee().getId().equals(actor.getId()) && actor.getRole() != Role.HR) {
            throw new AccessDeniedException("Only request owner can cancel");
        }

        LeaveStatus fromStatus = request.getStatus();
        request.setStatus(LeaveStatus.CANCELLED);

        // Release pending balance
        releasePendingBalance(request);

        // Mark pending steps as cancelled/rejected
        List<ApprovalStep> steps = approvalStepRepository.findByRequestIdOrderByIdAsc(request.getId());
        for (ApprovalStep s : steps) {
            if (s.getDecision() == ApprovalDecision.PENDING) {
                s.setDecision(ApprovalDecision.REJECTED);
                s.setDecidedAt(LocalDateTime.now());
                s.setComment("Cancelled by user");
                approvalStepRepository.save(s);
            }
        }

        createAuditEvent(request, actor, "CANCELLED", fromStatus, LeaveStatus.CANCELLED,
                comment != null ? comment : "Request cancelled by user");

        notificationService.notify(
                request.getEmployee(),
                "Your leave request #" + request.getId() + " has been cancelled.",
                request.getId(),
                "Leave Request Cancelled",
                "Your leave request #" + request.getId() + " has been successfully cancelled."
        );
    }

    private void handleReject(LeaveRequest request, User actor, String comment) {
        if (comment == null || comment.isBlank()) {
            throw new IllegalArgumentException("Comment is required when rejecting a request");
        }

        LeaveStatus fromStatus = request.getStatus();

        // Authority verification
        if (fromStatus == LeaveStatus.PENDING_MANAGER) {
            validateManagerAuthority(request, actor);
        } else if (fromStatus == LeaveStatus.PENDING_HR || fromStatus == LeaveStatus.ESCALATED) {
            if (actor != null && actor.getRole() != Role.HR) {
                throw new AccessDeniedException("Only HR can reject at stage " + fromStatus);
            }
        } else {
            throw new ConflictException("Cannot REJECT from " + fromStatus);
        }

        request.setStatus(LeaveStatus.REJECTED);

        // Release pending balance
        releasePendingBalance(request);

        // Update current pending step
        ApprovalStep step = findCurrentPendingStep(request);
        String auditComment = comment;
        if (step != null) {
            step.setDecision(ApprovalDecision.REJECTED);
            step.setDecidedAt(LocalDateTime.now());
            step.setComment(comment);
            if (step.getDelegatedFrom() != null && actor != null) {
                auditComment = "Rejected by " + actor.getName() + " on behalf of " + step.getDelegatedFrom().getName() + ": " + comment;
            }
            approvalStepRepository.save(step);
        }

        createAuditEvent(request, actor, "REJECTED", fromStatus, LeaveStatus.REJECTED, auditComment);

        notificationService.notify(
                request.getEmployee(),
                "Your leave request #" + request.getId() + " was rejected: " + comment,
                request.getId(),
                "Leave Request Rejected",
                "Your leave request has been rejected. Reason: " + comment
        );
    }

    private void handleApprove(LeaveRequest request, User actor, String comment) {
        LeaveStatus fromStatus = request.getStatus();

        if (fromStatus == LeaveStatus.PENDING_MANAGER) {
            validateManagerAuthority(request, actor);

            ApprovalStep step = findCurrentPendingStep(request);
            String auditComment = comment;
            if (step != null) {
                step.setDecision(ApprovalDecision.APPROVED);
                step.setDecidedAt(LocalDateTime.now());
                step.setComment(comment);
                if (step.getDelegatedFrom() != null && actor != null) {
                    auditComment = "Approved by " + actor.getName() + " on behalf of " + step.getDelegatedFrom().getName()
                            + (comment != null ? ": " + comment : "");
                }
                approvalStepRepository.save(step);
            }

            request.setStatus(LeaveStatus.PENDING_HR);

            // Create HR ApprovalStep (balance remains unchanged in PENDING)
            User hrUser = findHrUser();
            createApprovalStep(request, ApprovalStage.HR, hrUser, null);

            createAuditEvent(request, actor, "APPROVED_BY_MANAGER", fromStatus, LeaveStatus.PENDING_HR, auditComment);

            notificationService.notify(
                    request.getEmployee(),
                    "Your leave request #" + request.getId() + " was approved by manager and forwarded to HR.",
                    request.getId(),
                    "Leave Request Progressing",
                    "Manager has approved your leave request. It is now pending HR review."
            );

            if (hrUser != null) {
                notificationService.notify(
                        hrUser,
                        "Leave request #" + request.getId() + " for " + request.getEmployee().getName() + " is pending your review.",
                        request.getId(),
                        "Leave Request Pending HR Approval",
                        "A leave request has been approved by manager and is awaiting your final decision."
                );
            }

        } else if (fromStatus == LeaveStatus.PENDING_HR || fromStatus == LeaveStatus.ESCALATED) {
            if (actor != null && actor.getRole() != Role.HR) {
                throw new AccessDeniedException("Only HR can grant final approval for " + fromStatus);
            }

            ApprovalStep step = findCurrentPendingStep(request);
            if (step != null) {
                step.setDecision(ApprovalDecision.APPROVED);
                step.setDecidedAt(LocalDateTime.now());
                step.setComment(comment);
                approvalStepRepository.save(step);
            }

            request.setStatus(LeaveStatus.APPROVED);

            // Deduct balance ONLY on HR approval (pending -> used)
            deductBalanceOnFinalApproval(request);

            createAuditEvent(request, actor, "APPROVED", fromStatus, LeaveStatus.APPROVED,
                    comment != null ? comment : "Final approval granted by HR");

            notificationService.notify(
                    request.getEmployee(),
                    "Your leave request #" + request.getId() + " has been APPROVED!",
                    request.getId(),
                    "Leave Request Approved",
                    "Congratulations! Your leave request #" + request.getId() + " has received final approval."
            );

        } else {
            throw new ConflictException("Cannot APPROVE from " + fromStatus);
        }
    }

    private void handleEscalate(LeaveRequest request, String comment) {
        LeaveStatus fromStatus = request.getStatus();
        if (fromStatus != LeaveStatus.PENDING_MANAGER) {
            throw new ConflictException("Cannot ESCALATE from " + fromStatus);
        }

        // Close manager step
        ApprovalStep step = findCurrentPendingStep(request);
        if (step != null) {
            step.setDecision(ApprovalDecision.ESCALATED);
            step.setDecidedAt(LocalDateTime.now());
            step.setComment(comment != null ? comment : "SLA deadline exceeded; escalated to HR");
            approvalStepRepository.save(step);
        }

        request.setStatus(LeaveStatus.ESCALATED);

        User hrUser = findHrUser();
        createApprovalStep(request, ApprovalStage.HR, hrUser, null);

        createAuditEvent(request, null, "ESCALATED", fromStatus, LeaveStatus.ESCALATED,
                comment != null ? comment : "Auto-escalated to HR due to manager SLA timeout");

        if (hrUser != null) {
            notificationService.notify(
                    hrUser,
                    "Leave request #" + request.getId() + " has been ESCALATED to HR due to manager SLA timeout.",
                    request.getId(),
                    "Leave Request Escalated",
                    "Manager did not respond in time. Leave request #" + request.getId() + " is now escalated to HR."
            );
        }
    }

    private void validateManagerAuthority(LeaveRequest request, User actor) {
        if (actor == null) return;
        if (actor.getRole() == Role.HR) return; // HR can intervene

        User employee = request.getEmployee();
        User manager = employee.getManager();

        boolean isDirectManager = manager != null && manager.getId().equals(actor.getId());

        // Check if actor is an active delegate for the manager
        boolean isDelegate = false;
        if (manager != null) {
            DelegationService.ResolvedAssignee resolved = delegationService.resolveAssignee(manager, LocalDate.now());
            if (resolved.effectiveAssignee() != null && resolved.effectiveAssignee().getId().equals(actor.getId())) {
                isDelegate = true;
            }
        }

        if (!isDirectManager && !isDelegate) {
            throw new AccessDeniedException("You are not authorized to decide this request (only the manager or assigned delegate may decide)");
        }
    }

    private ApprovalStep findCurrentPendingStep(LeaveRequest request) {
        List<ApprovalStep> steps = approvalStepRepository.findByRequestIdOrderByIdAsc(request.getId());
        for (int i = steps.size() - 1; i >= 0; i--) {
            ApprovalStep s = steps.get(i);
            if (s.getDecision() == ApprovalDecision.PENDING) {
                return s;
            }
        }
        return null;
    }

    public ApprovalStep createApprovalStep(LeaveRequest request, ApprovalStage stage, User intendedAssignee, LocalDateTime dueAt) {
        DelegationService.ResolvedAssignee resolved = delegationService.resolveAssignee(intendedAssignee, LocalDate.now());
        User effectiveAssignee = resolved.effectiveAssignee();
        User delegatedFrom = resolved.delegatedFrom();

        ApprovalStep step = new ApprovalStep();
        step.setRequest(request);
        step.setStage(stage);
        step.setAssignee(effectiveAssignee);
        step.setDelegatedFrom(delegatedFrom);
        step.setDueAt(dueAt != null ? dueAt : LocalDateTime.now().plusMinutes(escalationTimeoutMinutes));
        step.setDecision(ApprovalDecision.PENDING);
        return approvalStepRepository.save(step);
    }

    private void releasePendingBalance(LeaveRequest request) {
        if ("UNPAID".equalsIgnoreCase(request.getLeaveType().getCode())) {
            return;
        }
        int year = request.getFromDate().getYear();
        leaveBalanceRepository.findByUserIdAndLeaveTypeIdAndYear(
                request.getEmployee().getId(), request.getLeaveType().getId(), year
        ).ifPresent(balance -> {
            balance.setPending(Math.max(0.0, balance.getPending() - request.getDays()));
            leaveBalanceRepository.save(balance);
        });
    }

    private void deductBalanceOnFinalApproval(LeaveRequest request) {
        if ("UNPAID".equalsIgnoreCase(request.getLeaveType().getCode())) {
            return;
        }
        int year = request.getFromDate().getYear();
        leaveBalanceRepository.findByUserIdAndLeaveTypeIdAndYear(
                request.getEmployee().getId(), request.getLeaveType().getId(), year
        ).ifPresent(balance -> {
            balance.setPending(Math.max(0.0, balance.getPending() - request.getDays()));
            balance.setUsed(balance.getUsed() + request.getDays());
            leaveBalanceRepository.save(balance);
        });
    }

    public AuditEvent createAuditEvent(LeaveRequest request, User actor, String action, LeaveStatus fromStatus, LeaveStatus toStatus, String comment) {
        AuditEvent event = new AuditEvent();
        event.setRequest(request);
        event.setActor(actor);
        event.setAction(action);
        event.setFromStatus(fromStatus);
        event.setToStatus(toStatus);
        event.setComment(comment);
        event.setAt(LocalDateTime.now());
        return auditEventRepository.save(event);
    }

    public User findHrUser() {
        return userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.HR)
                .findFirst()
                .orElse(null);
    }

    public LeaveResponseDto toDto(LeaveRequest r) {
        return new LeaveResponseDto(
                r.getId(),
                r.getEmployee().getId(),
                r.getEmployee().getName(),
                r.getEmployee().getEmail(),
                r.getLeaveType().getId(),
                r.getLeaveType().getCode(),
                r.getLeaveType().getName(),
                r.getFromDate(),
                r.getToDate(),
                r.getDays(),
                r.getReason(),
                r.getStatus(),
                r.isFlagged(),
                r.getFlagReason(),
                r.getCreatedAt(),
                r.getLastActionAt()
        );
    }
}
