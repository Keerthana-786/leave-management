package com.hackathon.leave.dto;

import com.hackathon.leave.model.ConflictSeverity;
import com.hackathon.leave.model.LeaveStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record LeaveResponseDto(
        Long id,
        Long employeeId,
        String employeeName,
        String employeeEmail,
        Long leaveTypeId,
        String leaveTypeCode,
        String leaveTypeName,
        LocalDate fromDate,
        LocalDate toDate,
        int days,
        String reason,
        LeaveStatus status,
        boolean flagged,
        String flagReason,
        LocalDateTime createdAt,
        LocalDateTime lastActionAt,
        ConflictSeverity conflictSeverity,
        Double employeeAvailableBalance,
        List<String> overlappingTeammates,
        LocalDateTime dueAt
) {
    // Backward-compatible constructor for 16-parameter usage
    public LeaveResponseDto(
            Long id,
            Long employeeId,
            String employeeName,
            String employeeEmail,
            Long leaveTypeId,
            String leaveTypeCode,
            String leaveTypeName,
            LocalDate fromDate,
            LocalDate toDate,
            int days,
            String reason,
            LeaveStatus status,
            boolean flagged,
            String flagReason,
            LocalDateTime createdAt,
            LocalDateTime lastActionAt
    ) {
        this(id, employeeId, employeeName, employeeEmail, leaveTypeId, leaveTypeCode, leaveTypeName,
                fromDate, toDate, days, reason, status, flagged, flagReason, createdAt, lastActionAt,
                null, null, null, null);
    }
}
