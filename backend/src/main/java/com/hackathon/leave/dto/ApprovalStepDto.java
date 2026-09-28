package com.hackathon.leave.dto;

import com.hackathon.leave.model.ApprovalDecision;
import com.hackathon.leave.model.ApprovalStage;

import java.time.LocalDateTime;

public record ApprovalStepDto(
        Long id,
        Long requestId,
        ApprovalStage stage,
        Long assigneeId,
        String assigneeName,
        Long delegatedFromId,
        String delegatedFromName,
        LocalDateTime dueAt,
        ApprovalDecision decision,
        LocalDateTime decidedAt,
        String comment
) {}
