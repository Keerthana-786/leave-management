package com.hackathon.leave.service.rule;

import com.hackathon.leave.model.User;
import java.time.LocalDate;

public interface ConflictStrategy {
    ConflictResult evaluate(User employee, LocalDate fromDate, LocalDate toDate, Long excludeRequestId);
}
