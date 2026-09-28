package com.hackathon.leave.controller;

import com.hackathon.leave.dto.DelegationCreateRequest;
import com.hackathon.leave.dto.DelegationDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/delegations")
@Tag(name = "Delegations", description = "Endpoints for managing temporary manager approval authority delegations")
public class DelegationController {

    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Create delegation", description = "Delegates manager approval authority to a peer or teammate for a date range")
    public ResponseEntity<DelegationDto> createDelegation(@Valid @RequestBody DelegationCreateRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'HR')")
    @Operation(summary = "Get active delegations", description = "Retrieves active delegation rules created by or assigned to the current manager")
    public ResponseEntity<List<DelegationDto>> getDelegations() {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Revoke delegation", description = "Deactivates and revokes an existing approval authority delegation")
    public ResponseEntity<Void> revokeDelegation(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }
}
