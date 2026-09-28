package com.hackathon.leave.service.rule;

import com.hackathon.leave.model.ConflictSeverity;
import com.hackathon.leave.model.User;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class ConflictEvaluator {

    private final List<ConflictStrategy> strategies;

    public ConflictEvaluator(List<ConflictStrategy> strategies) {
        this.strategies = strategies != null ? strategies : List.of();
    }

    public ConflictResult evaluate(User employee, LocalDate fromDate, LocalDate toDate, Long excludeRequestId) {
        ConflictSeverity worstSeverity = ConflictSeverity.NONE;
        boolean flagged = false;
        List<String> reasons = new ArrayList<>();
        List<String> allTeammates = new ArrayList<>();

        for (ConflictStrategy strategy : strategies) {
            ConflictResult result = strategy.evaluate(employee, fromDate, toDate, excludeRequestId);
            if (result != null && result.severity() != null) {
                if (result.severity().ordinal() > worstSeverity.ordinal()) {
                    worstSeverity = result.severity();
                }
                if (result.flagged()) {
                    flagged = true;
                }
                if (result.reason() != null && !result.reason().isBlank()) {
                    reasons.add(result.reason());
                }
                if (result.overlappingTeammates() != null) {
                    for (String name : result.overlappingTeammates()) {
                        if (!allTeammates.contains(name)) {
                            allTeammates.add(name);
                        }
                    }
                }
            }
        }

        String combinedReason = reasons.isEmpty() ? null : String.join("; ", reasons);
        return new ConflictResult(worstSeverity, flagged, combinedReason, allTeammates);
    }
}
