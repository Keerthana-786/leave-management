package com.hackathon.leave.dto;

import java.util.List;

public record LeavePreviewResponse(
        int workingDays,
        double currentBalance,
        double balanceAfter,
        boolean conflictFlagged,
        String flagReason,
        List<String> holidayDates
) {}
