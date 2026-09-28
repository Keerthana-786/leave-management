package com.hackathon.leave.dto;

import java.util.Map;

public record AnalyticsSummaryDto(
        long totalRequests,
        long approvedRequests,
        long pendingManager,
        long pendingHr,
        long escalated,
        long rejected,
        long cancelled,
        double averageDaysPerRequest,
        Map<String, Long> requestsByType
) {}
