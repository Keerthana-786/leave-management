package com.hackathon.leave.dto;

import com.hackathon.leave.model.LeaveStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
        LocalDateTime lastActionAt
) {}
