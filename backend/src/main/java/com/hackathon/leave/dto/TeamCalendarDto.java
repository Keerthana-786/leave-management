package com.hackathon.leave.dto;

import java.util.List;

public record TeamCalendarDto(
        Long teamId,
        String teamName,
        String month,
        List<TeamLeaveEventDto> events
) {}
