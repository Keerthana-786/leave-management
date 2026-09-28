package com.hackathon.leave.service.rule;

import com.hackathon.leave.model.ConflictSeverity;
import com.hackathon.leave.model.LeaveRequest;
import com.hackathon.leave.model.LeaveStatus;
import com.hackathon.leave.model.User;
import com.hackathon.leave.repository.LeaveRequestRepository;
import com.hackathon.leave.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Order(1)
public class HeadcountConflictStrategy implements ConflictStrategy {

    private final UserRepository userRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final double thresholdLow;
    private final double thresholdMedium;
    private final double thresholdHigh;

    private static final List<LeaveStatus> ACTIVE_STATUSES = List.of(
            LeaveStatus.PENDING_MANAGER,
            LeaveStatus.PENDING_HR,
            LeaveStatus.ESCALATED,
            LeaveStatus.APPROVED
    );

    public HeadcountConflictStrategy(
            UserRepository userRepository,
            LeaveRequestRepository leaveRequestRepository,
            @Value("${leave.conflict.threshold-low:0.30}") double thresholdLow,
            @Value("${leave.conflict.threshold-medium:0.50}") double thresholdMedium,
            @Value("${leave.conflict.threshold-high:0.70}") double thresholdHigh
    ) {
        this.userRepository = userRepository;
        this.leaveRequestRepository = leaveRequestRepository;
        this.thresholdLow = thresholdLow;
        this.thresholdMedium = thresholdMedium;
        this.thresholdHigh = thresholdHigh;
    }

    @Override
    public ConflictResult evaluate(User employee, LocalDate fromDate, LocalDate toDate, Long excludeRequestId) {
        if (employee == null || employee.getTeam() == null) {
            return ConflictResult.none();
        }

        List<User> teamMembers = userRepository.findByTeamId(employee.getTeam().getId());
        List<User> teammates = teamMembers.stream()
                .filter(u -> !u.getId().equals(employee.getId()))
                .toList();

        int totalTeammates = teammates.size();
        if (totalTeammates == 0) {
            return ConflictResult.none();
        }

        List<Long> teammateIds = teammates.stream().map(User::getId).toList();
        List<LeaveRequest> overlapping = leaveRequestRepository.findOverlappingRequests(
                teammateIds, ACTIVE_STATUSES, fromDate, toDate
        );

        Set<Long> onLeaveTeammateIds = overlapping.stream()
                .filter(r -> excludeRequestId == null || !r.getId().equals(excludeRequestId))
                .map(r -> r.getEmployee().getId())
                .collect(Collectors.toSet());

        List<String> onLeaveNames = teammates.stream()
                .filter(u -> onLeaveTeammateIds.contains(u.getId()))
                .map(User::getName)
                .sorted()
                .toList();

        int overlappingCount = onLeaveNames.size();
        double coverage = (double) overlappingCount / (double) totalTeammates;

        ConflictSeverity severity;
        if (coverage >= thresholdHigh) {
            severity = ConflictSeverity.HIGH;
        } else if (coverage >= thresholdMedium) {
            severity = ConflictSeverity.MEDIUM;
        } else if (coverage >= thresholdLow) {
            severity = ConflictSeverity.LOW;
        } else {
            severity = ConflictSeverity.NONE;
        }

        if (severity == ConflictSeverity.NONE) {
            return ConflictResult.none();
        }

        String reason = String.format("%d of %d teammates on leave: %s",
                overlappingCount, totalTeammates, String.join(", ", onLeaveNames));

        return new ConflictResult(severity, true, reason, onLeaveNames);
    }
}
