package com.hackathon.leave.dto;

public record AuthResponse(
        String token,
        String tokenType,
        UserSummaryDto user
) {
    public AuthResponse(String token, UserSummaryDto user) {
        this(token, "Bearer", user);
    }
}
