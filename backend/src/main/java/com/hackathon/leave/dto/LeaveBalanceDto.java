package com.hackathon.leave.dto;

public record LeaveBalanceDto(
        Long id,
        Long leaveTypeId,
        String leaveTypeCode,
        String leaveTypeName,
        int year,
        double entitled,
        double used,
        double pending,
        double remaining
) {}
