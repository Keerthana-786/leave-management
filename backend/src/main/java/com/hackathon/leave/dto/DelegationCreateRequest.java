package com.hackathon.leave.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record DelegationCreateRequest(
        @NotNull Long delegateId,
        @NotNull LocalDate fromDate,
        @NotNull LocalDate toDate
) {}
