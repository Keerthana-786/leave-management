package com.hackathon.leave.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record LeavePreviewRequest(
        @NotNull Long leaveTypeId,
        @NotNull LocalDate fromDate,
        @NotNull LocalDate toDate
) {}
