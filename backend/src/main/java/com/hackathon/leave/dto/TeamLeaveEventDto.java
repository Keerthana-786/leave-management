package com.hackathon.leave.dto;

import com.hackathon.leave.model.LeaveStatus;

import java.time.LocalDate;

public record TeamLeaveEventDto(
        Long leaveId,
        Long employeeId,
        String employeeName,
        LocalDate fromDate,
        LocalDate toDate,
        LeaveStatus status,
        String leaveTypeCode
) {}
