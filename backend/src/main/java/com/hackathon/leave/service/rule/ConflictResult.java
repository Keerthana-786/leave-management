package com.hackathon.leave.service.rule;

import com.hackathon.leave.model.ConflictSeverity;
import java.util.Collections;
import java.util.List;

public record ConflictResult(
        ConflictSeverity severity,
        boolean flagged,
        String reason,
        List<String> overlappingTeammates
) {
    public static ConflictResult none() {
        return new ConflictResult(ConflictSeverity.NONE, false, null, Collections.emptyList());
    }
}
