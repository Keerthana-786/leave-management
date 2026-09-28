package com.hackathon.leave.controller;

import com.hackathon.leave.dto.DelegationCreateRequest;
import com.hackathon.leave.dto.DelegationDto;
import com.hackathon.leave.model.User;
import com.hackathon.leave.security.SecurityUtils;
import com.hackathon.leave.service.DelegationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/delegations")
@Tag(name = "Delegations", description = "Endpoints for managing temporary manager approval authority delegations")
public class DelegationController {

    private final DelegationService delegationService;
    private final SecurityUtils securityUtils;

    public DelegationController(DelegationService delegationService, SecurityUtils securityUtils) {
        this.delegationService = delegationService;
        this.securityUtils = securityUtils;
    }

    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Create delegation", description = "Delegates manager approval authority to a peer or teammate for a date range")
    public ResponseEntity<DelegationDto> createDelegation(@Valid @RequestBody DelegationCreateRequest request) {
        User currentUser = securityUtils.getCurrentUser();
        DelegationDto created = delegationService.createDelegation(currentUser, request);
        return ResponseEntity.ok(created);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'HR')")
    @Operation(summary = "Get active delegations", description = "Retrieves active delegation rules created by or assigned to the current manager")
    public ResponseEntity<List<DelegationDto>> getDelegations() {
        User currentUser = securityUtils.getCurrentUser();
        List<DelegationDto> delegations = delegationService.getDelegations(currentUser);
        return ResponseEntity.ok(delegations);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Revoke delegation", description = "Deactivates and revokes an existing approval authority delegation")
    public ResponseEntity<Void> revokeDelegation(@PathVariable Long id) {
        User currentUser = securityUtils.getCurrentUser();
        delegationService.revokeDelegation(currentUser, id);
        return ResponseEntity.noContent().build();
    }
}
