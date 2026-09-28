package com.hackathon.leave.service;

import com.hackathon.leave.dto.*;
import com.hackathon.leave.exception.BusinessRuleException;
import com.hackathon.leave.exception.ResourceNotFoundException;
import com.hackathon.leave.model.*;
import com.hackathon.leave.repository.*;
import com.hackathon.leave.service.rule.ConflictEvaluator;
import com.hackathon.leave.service.rule.ConflictResult;
import com.hackathon.leave.service.rule.WorkingDayCalculator;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
public class LeaveService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final ApprovalStepRepository approvalStepRepository;
    private final AuditEventRepository auditEventRepository;
    private final WorkingDayCalculator workingDayCalculator;
    private final ConflictEvaluator conflictEvaluator;
    private final WorkflowService workflowService;
    private final NotificationService notificationService;

    private static final List<LeaveStatus> ACTIVE_STATUSES = List.of(
            LeaveStatus.PENDING_MANAGER,
            LeaveStatus.PENDING_HR,
            LeaveStatus.ESCALATED,
            LeaveStatus.APPROVED
    );

    public LeaveService(
            LeaveRequestRepository leaveRequestRepository,
            LeaveTypeRepository leaveTypeRepository,
            LeaveBalanceRepository leaveBalanceRepository,
            ApprovalStepRepository approvalStepRepository,
            AuditEventRepository auditEventRepository,
            WorkingDayCalculator workingDayCalculator,
            ConflictEvaluator conflictEvaluator,
            WorkflowService workflowService,
            NotificationService notificationService
    ) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.leaveTypeRepository = leaveTypeRepository;
        this.leaveBalanceRepository = leaveBalanceRepository;
        this.approvalStepRepository = approvalStepRepository;
        this.auditEventRepository = auditEventRepository;
        this.workingDayCalculator = workingDayCalculator;
        this.conflictEvaluator = conflictEvaluator;
        this.workflowService = workflowService;
        this.notificationService = notificationService;
    }

    public record EvaluationResult(
            int workingDays,
            double currentBalance,
            double balanceAfter,
            ConflictResult conflictResult,
            List<String> holidayDates,
            LeaveType leaveType,
            LeaveBalance balance
    ) {}

    public EvaluationResult evaluate(User employee, Long leaveTypeId, LocalDate fromDate, LocalDate toDate) {
        if (leaveTypeId == null) {
            throw new BusinessRuleException("Leave type ID is required");
        }
        LeaveType leaveType = leaveTypeRepository.findById(leaveTypeId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave type not found with id: " + leaveTypeId));

        // 1. Dates and Working days check (Mon-Fri minus Holidays; 0 working days -> 422)
        WorkingDayCalculator.WorkingDayResult wdResult = workingDayCalculator.calculate(fromDate, toDate);
        int workingDays = wdResult.workingDays();

        // 2. Own overlap check -> 422
        List<LeaveRequest> ownOverlaps = leaveRequestRepository.findOwnOverlappingRequests(
                employee.getId(), ACTIVE_STATUSES, fromDate, toDate
        );
        if (!ownOverlaps.isEmpty()) {
            throw new BusinessRuleException("You already have an existing active leave request for overlapping dates");
        }

        // 3. Balance check -> 422 if insufficient
        double currentBalance = 0.0;
        double balanceAfter = 0.0;
        LeaveBalance balance = null;

        if (!"UNPAID".equalsIgnoreCase(leaveType.getCode()) && leaveType.isPaid()) {
            int year = fromDate.getYear();
            balance = leaveBalanceRepository.findByUserIdAndLeaveTypeIdAndYear(employee.getId(), leaveTypeId, year)
                    .orElseThrow(() -> new BusinessRuleException("No leave balance record found for " + leaveType.getName() + " in year " + year));

            double available = balance.getEntitled() - balance.getUsed() - balance.getPending();
            if (workingDays > available) {
                throw new BusinessRuleException(String.format(
                        "Insufficient leave balance for %s. Requested: %d days, Available: %.1f days",
                        leaveType.getName(), workingDays, available
                ));
            }
            currentBalance = available;
            balanceAfter = available - workingDays;
        }

        // 4. Conflict evaluation (FLAG only, NEVER reject)
        ConflictResult conflict = conflictEvaluator.evaluate(employee, fromDate, toDate, null);

        return new EvaluationResult(
                workingDays,
                currentBalance,
                balanceAfter,
                conflict,
                wdResult.holidayDates(),
                leaveType,
                balance
        );
    }

    @Transactional(readOnly = true)
    public LeavePreviewResponse preview(User employee, LeavePreviewRequest request) {
        EvaluationResult eval = evaluate(employee, request.leaveTypeId(), request.fromDate(), request.toDate());
        return new LeavePreviewResponse(
                eval.workingDays(),
                eval.currentBalance(),
                eval.balanceAfter(),
                eval.conflictResult().flagged(),
                eval.conflictResult().reason(),
                eval.holidayDates()
        );
    }

    @Transactional
    public LeaveResponseDto apply(User employee, LeaveApplyRequest request) {
        EvaluationResult eval = evaluate(employee, request.leaveTypeId(), request.fromDate(), request.toDate());

        // Reserve pending balance
        if (eval.balance() != null) {
            LeaveBalance bal = eval.balance();
            bal.setPending(bal.getPending() + eval.workingDays());
            leaveBalanceRepository.save(bal);
        }

        // Determine workflow stage and approver
        // A manager's own leave goes directly to HR
        LeaveStatus initialStatus;
        ApprovalStage initialStage;
        User approver;

        if (employee.getRole() == Role.MANAGER || employee.getManager() == null) {
            initialStatus = LeaveStatus.PENDING_HR;
            initialStage = ApprovalStage.HR;
            approver = workflowService.findHrUser();
        } else {
            initialStatus = LeaveStatus.PENDING_MANAGER;
            initialStage = ApprovalStage.MANAGER;
            approver = employee.getManager();
        }

        // Persist LeaveRequest
        LeaveRequest leaveRequest = new LeaveRequest();
        leaveRequest.setEmployee(employee);
        leaveRequest.setLeaveType(eval.leaveType());
        leaveRequest.setFromDate(request.fromDate());
        leaveRequest.setToDate(request.toDate());
        leaveRequest.setDays(eval.workingDays());
        leaveRequest.setReason(request.reason());
        leaveRequest.setStatus(initialStatus);
        leaveRequest.setFlagged(eval.conflictResult().flagged());
        leaveRequest.setFlagReason(eval.conflictResult().reason());
        leaveRequest.setCreatedAt(LocalDateTime.now());
        leaveRequest.setLastActionAt(LocalDateTime.now());

        LeaveRequest saved = leaveRequestRepository.save(leaveRequest);

        // Create initial ApprovalStep
        ApprovalStep step = workflowService.createApprovalStep(saved, initialStage, approver, null);

        // Create AuditEvent SUBMITTED
        workflowService.createAuditEvent(saved, employee, "SUBMITTED", null, initialStatus,
                "Leave request submitted: " + request.reason());

        // Notify approver
        if (step.getAssignee() != null) {
            notificationService.notify(
                    step.getAssignee(),
                    "New leave request submitted by " + employee.getName() + " (" + eval.workingDays() + " days)",
                    saved.getId(),
                    "New Leave Request Submitted",
                    "A new leave request requires your review from " + employee.getName() + "."
            );
        }

        return workflowService.toDto(saved);
    }

    @Transactional(readOnly = true)
    public List<LeaveResponseDto> getMyLeaves(User employee) {
        return leaveRequestRepository.findByEmployeeId(employee.getId()).stream()
                .sorted(Comparator.comparing(LeaveRequest::getCreatedAt).reversed())
                .map(workflowService::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public LeaveResponseDto getLeaveById(User currentUser, Long id) {
        LeaveRequest request = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found with id: " + id));

        validateViewAccess(currentUser, request);
        return workflowService.toDto(request);
    }

    @Transactional(readOnly = true)
    public LeaveTimelineDto getLeaveTimeline(User currentUser, Long id) {
        LeaveRequest request = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found with id: " + id));

        validateViewAccess(currentUser, request);

        List<ApprovalStep> steps = approvalStepRepository.findByRequestIdOrderByIdAsc(request.getId());
        List<ApprovalStepDto> stepDtos = steps.stream().map(s -> new ApprovalStepDto(
                s.getId(),
                s.getRequest().getId(),
                s.getStage(),
                s.getAssignee() != null ? s.getAssignee().getId() : null,
                s.getAssignee() != null ? s.getAssignee().getName() : null,
                s.getDelegatedFrom() != null ? s.getDelegatedFrom().getId() : null,
                s.getDelegatedFrom() != null ? s.getDelegatedFrom().getName() : null,
                s.getDueAt(),
                s.getDecision(),
                s.getDecidedAt(),
                s.getComment()
        )).toList();

        List<AuditEvent> events = auditEventRepository.findByRequestIdOrderByAtDesc(request.getId());
        List<AuditEventDto> eventDtos = events.stream().map(e -> new AuditEventDto(
                e.getId(),
                e.getRequest().getId(),
                e.getActor() != null ? e.getActor().getId() : null,
                e.getActor() != null ? e.getActor().getName() : "SYSTEM", // null actor = SYSTEM
                e.getAction(),
                e.getFromStatus(),
                e.getToStatus(),
                e.getComment(),
                e.getAt()
        )).toList();

        return new LeaveTimelineDto(workflowService.toDto(request), stepDtos, eventDtos);
    }

    @Transactional
    public LeaveResponseDto cancelLeave(User currentUser, Long id) {
        return workflowService.transition(id, WorkflowService.Action.CANCEL, currentUser, "Cancelled by user");
    }

    @Transactional
    public LeaveResponseDto approveLeave(User currentUser, Long id, ApprovalDecisionRequest request) {
        String comment = request != null ? request.comment() : null;
        return workflowService.transition(id, WorkflowService.Action.APPROVE, currentUser, comment);
    }

    @Transactional
    public LeaveResponseDto rejectLeave(User currentUser, Long id, ApprovalDecisionRequest request) {
        String comment = request != null ? request.comment() : null;
        return workflowService.transition(id, WorkflowService.Action.REJECT, currentUser, comment);
    }

    private void validateViewAccess(User currentUser, LeaveRequest request) {
        if (currentUser.getRole() == Role.HR) {
            return;
        }
        if (currentUser.getRole() == Role.EMPLOYEE) {
            if (!request.getEmployee().getId().equals(currentUser.getId())) {
                throw new AccessDeniedException("You can only view your own leave requests");
            }
        }
        if (currentUser.getRole() == Role.MANAGER) {
            boolean isOwn = request.getEmployee().getId().equals(currentUser.getId());
            boolean isReport = request.getEmployee().getManager() != null &&
                    request.getEmployee().getManager().getId().equals(currentUser.getId());
            boolean isSameTeam = currentUser.getTeam() != null &&
                    request.getEmployee().getTeam() != null &&
                    request.getEmployee().getTeam().getId().equals(currentUser.getTeam().getId());
            if (!isOwn && !isReport && !isSameTeam) {
                throw new AccessDeniedException("You are not authorized to view this leave request");
            }
        }
    }
}
