package com.hackathon.leave.dto;

import java.util.List;

public record LeaveTimelineDto(
        LeaveResponseDto leave,
        List<ApprovalStepDto> steps,
        List<AuditEventDto> auditEvents
) {}
