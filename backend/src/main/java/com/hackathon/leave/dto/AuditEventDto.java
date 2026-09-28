package com.hackathon.leave.dto;

import com.hackathon.leave.model.LeaveStatus;

import java.time.LocalDateTime;

public record AuditEventDto(
        Long id,
        Long requestId,
        Long actorId,
        String actorName,
        String action,
        LeaveStatus fromStatus,
        LeaveStatus toStatus,
        String comment,
        LocalDateTime at
) {}
