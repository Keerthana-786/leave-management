package com.hackathon.leave.service;

import com.hackathon.leave.dto.LeaveResponseDto;
import com.hackathon.leave.dto.PageResponseDto;
import com.hackathon.leave.model.*;
import com.hackathon.leave.repository.*;
import com.hackathon.leave.service.rule.ConflictEvaluator;
import com.hackathon.leave.service.rule.ConflictResult;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ManagerHrService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final UserRepository userRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final ApprovalStepRepository approvalStepRepository;
    private final DelegationRepository delegationRepository;
    private final ConflictEvaluator conflictEvaluator;

    public ManagerHrService(
            LeaveRequestRepository leaveRequestRepository,
            UserRepository userRepository,
            LeaveBalanceRepository leaveBalanceRepository,
            ApprovalStepRepository approvalStepRepository,
            DelegationRepository delegationRepository,
            ConflictEvaluator conflictEvaluator
    ) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.userRepository = userRepository;
        this.leaveBalanceRepository = leaveBalanceRepository;
        this.approvalStepRepository = approvalStepRepository;
        this.delegationRepository = delegationRepository;
        this.conflictEvaluator = conflictEvaluator;
    }

    @Transactional(readOnly = true)
    public PageResponseDto<LeaveResponseDto> getManagerRequests(
            User manager,
            LeaveStatus status,
            String type,
            LocalDate from,
            LocalDate to,
            String q,
            String sortStr,
            int page,
            int size
    ) {
        // Collect assignable employee IDs:
        // 1. Direct reports
        Set<Long> employeeIds = userRepository.findByManagerId(manager.getId()).stream()
                .map(User::getId)
                .collect(Collectors.toSet());

        // 2. Team members if manager has a team
        if (manager.getTeam() != null) {
            userRepository.findByTeamId(manager.getTeam().getId()).stream()
                    .filter(u -> !u.getId().equals(manager.getId()))
                    .forEach(u -> employeeIds.add(u.getId()));
        }

        // 3. Delegated to me: active delegations where delegate = manager
        LocalDate today = LocalDate.now();
        List<Delegation> delegations = delegationRepository.findByDelegateIdAndActiveTrue(manager.getId());
        for (Delegation d : delegations) {
            if (!today.isBefore(d.getFromDate()) && !today.isAfter(d.getToDate())) {
                userRepository.findByManagerId(d.getDelegator().getId()).stream()
                        .map(User::getId)
                        .forEach(employeeIds::add);
            }
        }

        // Fetch all leave requests and filter in-memory for rich composite evaluation
        List<LeaveRequest> allRequests = leaveRequestRepository.findAll();

        List<LeaveRequest> filtered = allRequests.stream()
                .filter(r -> employeeIds.contains(r.getEmployee().getId()))
                .filter(r -> {
                    if (status != null) {
                        return r.getStatus() == status;
                    }
                    // default: team pending
                    return r.getStatus() == LeaveStatus.PENDING_MANAGER;
                })
                .filter(r -> type == null || type.isBlank() || r.getLeaveType().getCode().equalsIgnoreCase(type))
                .filter(r -> from == null || !r.getFromDate().isBefore(from))
                .filter(r -> to == null || !r.getToDate().isAfter(to))
                .filter(r -> {
                    if (q == null || q.isBlank()) return true;
                    String query = q.toLowerCase();
                    return r.getEmployee().getName().toLowerCase().contains(query)
                            || (r.getReason() != null && r.getReason().toLowerCase().contains(query));
                })
                .sorted(parseSort(sortStr))
                .toList();

        return paginate(filtered, page, size);
    }

    @Transactional(readOnly = true)
    public PageResponseDto<LeaveResponseDto> getHrRequests(
            LeaveStatus status,
            String type,
            LocalDate from,
            LocalDate to,
            String q,
            String sortStr,
            int page,
            int size
    ) {
        List<LeaveRequest> allRequests = leaveRequestRepository.findAll();

        List<LeaveRequest> filtered = allRequests.stream()
                .filter(r -> {
                    if (status != null) {
                        return r.getStatus() == status;
                    }
                    // default: PENDING_HR and ESCALATED
                    return r.getStatus() == LeaveStatus.PENDING_HR || r.getStatus() == LeaveStatus.ESCALATED;
                })
                .filter(r -> type == null || type.isBlank() || r.getLeaveType().getCode().equalsIgnoreCase(type))
                .filter(r -> from == null || !r.getFromDate().isBefore(from))
                .filter(r -> to == null || !r.getToDate().isAfter(to))
                .filter(r -> {
                    if (q == null || q.isBlank()) return true;
                    String query = q.toLowerCase();
                    return r.getEmployee().getName().toLowerCase().contains(query)
                            || r.getEmployee().getEmail().toLowerCase().contains(query)
                            || (r.getReason() != null && r.getReason().toLowerCase().contains(query));
                })
                .sorted(parseSort(sortStr))
                .toList();

        return paginate(filtered, page, size);
    }

    private PageResponseDto<LeaveResponseDto> paginate(List<LeaveRequest> requests, int page, int size) {
        int totalElements = requests.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        if (totalPages == 0) totalPages = 1;

        int fromIndex = Math.min(page * size, totalElements);
        int toIndex = Math.min(fromIndex + size, totalElements);

        List<LeaveResponseDto> content = requests.subList(fromIndex, toIndex).stream()
                .map(this::enrichDto)
                .toList();

        boolean isLast = (page + 1) >= totalPages;

        return new PageResponseDto<>(
                content,
                page,
                size,
                totalElements,
                totalPages,
                isLast
        );
    }

    private LeaveResponseDto enrichDto(LeaveRequest r) {
        // DueAt from active pending approval step
        LocalDateTime dueAt = null;
        List<ApprovalStep> steps = approvalStepRepository.findByRequestIdOrderByIdAsc(r.getId());
        for (int i = steps.size() - 1; i >= 0; i--) {
            ApprovalStep step = steps.get(i);
            if (step.getDecision() == ApprovalDecision.PENDING) {
                dueAt = step.getDueAt();
                break;
            }
        }

        // Available balance snapshot
        Double availableBalance = null;
        if (!"UNPAID".equalsIgnoreCase(r.getLeaveType().getCode())) {
            Optional<LeaveBalance> balOpt = leaveBalanceRepository.findByUserIdAndLeaveTypeIdAndYear(
                    r.getEmployee().getId(), r.getLeaveType().getId(), r.getFromDate().getYear()
            );
            if (balOpt.isPresent()) {
                LeaveBalance bal = balOpt.get();
                availableBalance = bal.getEntitled() - bal.getUsed() - bal.getPending();
            }
        }

        // Conflict info
        ConflictResult conflict = conflictEvaluator.evaluate(r.getEmployee(), r.getFromDate(), r.getToDate(), r.getId());

        ConflictSeverity severity = conflict.severity();
        List<String> overlappingTeammates = conflict.overlappingTeammates();
        boolean flagged = r.isFlagged() || conflict.flagged();
        String flagReason = r.getFlagReason() != null ? r.getFlagReason() : conflict.reason();

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
                flagged,
                flagReason,
                r.getCreatedAt(),
                r.getLastActionAt(),
                severity,
                availableBalance,
                overlappingTeammates,
                dueAt
        );
    }

    private Comparator<LeaveRequest> parseSort(String sortStr) {
        if (sortStr == null || sortStr.isBlank()) {
            return Comparator.comparing(LeaveRequest::getCreatedAt).reversed();
        }
        String[] parts = sortStr.split(",");
        String property = parts[0].trim();
        boolean desc = parts.length > 1 && "desc".equalsIgnoreCase(parts[1].trim());

        Comparator<LeaveRequest> comp;
        switch (property) {
            case "fromDate" -> comp = Comparator.comparing(LeaveRequest::getFromDate);
            case "toDate" -> comp = Comparator.comparing(LeaveRequest::getToDate);
            case "days" -> comp = Comparator.comparingInt(LeaveRequest::getDays);
            case "employee.name", "employeeName" -> comp = Comparator.comparing(r -> r.getEmployee().getName());
            default -> comp = Comparator.comparing(LeaveRequest::getCreatedAt);
        }

        return desc ? comp.reversed() : comp;
    }
}
