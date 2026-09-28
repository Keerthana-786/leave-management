package com.hackathon.leave.controller;

import com.hackathon.leave.dto.TeamCalendarDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
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

    @GetMapping("/team")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get team leave calendar", description = "Retrieves leaves scheduled across team members for a given month (YYYY-MM)")
    public ResponseEntity<TeamCalendarDto> getTeamCalendar(
            @Parameter(description = "Year and month in format YYYY-MM e.g. 2026-10", example = "2026-10")
            @RequestParam(required = false) String month
    ) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }
}
