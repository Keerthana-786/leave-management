package com.hackathon.leave.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record LeaveApplyRequest(
        @NotNull Long leaveTypeId,
        @NotNull LocalDate fromDate,
        @NotNull LocalDate toDate,
        @NotBlank String reason
) {}
