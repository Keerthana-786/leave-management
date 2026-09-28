package com.hackathon.leave.controller;

import com.hackathon.leave.dto.TeamCalendarDto;
import com.hackathon.leave.model.User;
import com.hackathon.leave.security.SecurityUtils;
import com.hackathon.leave.service.ExtraService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/calendar")
@Tag(name = "Calendar", description = "Endpoints for visualizing team leave schedules on a monthly calendar")
public class CalendarController {

    private final ExtraService extraService;
    private final SecurityUtils securityUtils;

    public CalendarController(ExtraService extraService, SecurityUtils securityUtils) {
        this.extraService = extraService;
        this.securityUtils = securityUtils;
    }

    @GetMapping("/team")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get team leave calendar", description = "Retrieves leaves scheduled across team members for a given month (YYYY-MM)")
    public ResponseEntity<TeamCalendarDto> getTeamCalendar(
            @Parameter(description = "Year and month in format YYYY-MM e.g. 2026-10", example = "2026-10")
            @RequestParam(required = false) String month
    ) {
        User currentUser = securityUtils.getCurrentUser();
        TeamCalendarDto calendar = extraService.getTeamCalendar(currentUser, month);
        return ResponseEntity.ok(calendar);
    }
}
