package com.hackathon.leave.controller;

import com.hackathon.leave.dto.HolidayCreateRequest;
import com.hackathon.leave.dto.HolidayDto;
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

import java.util.List;

@RestController
@RequestMapping("/api/holidays")
@Tag(name = "Holidays", description = "Endpoints for retrieving and managing public organization holidays")
public class HolidayController {

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get official holidays", description = "Retrieves the list of scheduled public holidays used for working day calculations")
    public ResponseEntity<List<HolidayDto>> getHolidays() {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @PostMapping
    @PreAuthorize("hasRole('HR')")
    @Operation(summary = "Add public holiday", description = "Creates a new public holiday (restricted to HR administrators)")
    public ResponseEntity<HolidayDto> createHoliday(@Valid @RequestBody HolidayCreateRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }
}
