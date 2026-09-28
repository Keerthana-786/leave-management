package com.hackathon.leave.controller;

import com.hackathon.leave.dto.AuthResponse;
import com.hackathon.leave.dto.LoginRequest;
import com.hackathon.leave.dto.UserSummaryDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Endpoints for user login and current session profile")
public class AuthController {

    @PostMapping("/login")
    @Operation(summary = "Authenticate user", description = "Validates user credentials and issues a JWT token")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get current user profile", description = "Retrieves profile and role details of the authenticated user")
    public ResponseEntity<UserSummaryDto> getCurrentUser() {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }
}
