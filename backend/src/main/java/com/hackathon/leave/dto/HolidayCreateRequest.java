package com.hackathon.leave.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record HolidayCreateRequest(
        @NotNull LocalDate date,
        @NotBlank String name
) {}
