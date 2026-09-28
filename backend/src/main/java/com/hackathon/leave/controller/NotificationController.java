package com.hackathon.leave.controller;

import com.hackathon.leave.dto.NotificationDto;
import com.hackathon.leave.model.User;
import com.hackathon.leave.security.SecurityUtils;
import com.hackathon.leave.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Notifications", description = "Endpoints for viewing and acknowledging in-app notification alerts")
public class NotificationController {

    private final NotificationService notificationService;
    private final SecurityUtils securityUtils;

    public NotificationController(NotificationService notificationService, SecurityUtils securityUtils) {
        this.notificationService = notificationService;
        this.securityUtils = securityUtils;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get user notifications", description = "Retrieves in-app notifications for the authenticated user, ordered by creation date descending")
    public ResponseEntity<List<NotificationDto>> getNotifications() {
        User currentUser = securityUtils.getCurrentUser();
        List<NotificationDto> notifications = notificationService.getMyNotifications(currentUser);
        return ResponseEntity.ok(notifications);
    }

    @PostMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Mark notification as read", description = "Updates an in-app notification status to read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        User currentUser = securityUtils.getCurrentUser();
        notificationService.markAsRead(currentUser, id);
        return ResponseEntity.ok().build();
    }
}
