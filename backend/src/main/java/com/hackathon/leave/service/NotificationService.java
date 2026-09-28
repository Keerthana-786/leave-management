package com.hackathon.leave.service;

import com.hackathon.leave.dto.NotificationDto;
import com.hackathon.leave.exception.ResourceNotFoundException;
import com.hackathon.leave.model.EmailOutbox;
import com.hackathon.leave.model.LeaveRequest;
import com.hackathon.leave.model.Notification;
import com.hackathon.leave.model.User;
import com.hackathon.leave.repository.EmailOutboxRepository;
import com.hackathon.leave.repository.LeaveRequestRepository;
import com.hackathon.leave.repository.NotificationRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmailOutboxRepository emailOutboxRepository;
    private final LeaveRequestRepository leaveRequestRepository;

    public NotificationService(
            NotificationRepository notificationRepository,
            EmailOutboxRepository emailOutboxRepository,
            LeaveRequestRepository leaveRequestRepository
    ) {
        this.notificationRepository = notificationRepository;
        this.emailOutboxRepository = emailOutboxRepository;
        this.leaveRequestRepository = leaveRequestRepository;
    }

    @Transactional
    public void notify(User recipient, String message, Long relatedRequestId, String emailSubject, String emailBody) {
        if (recipient == null) {
            return;
        }

        LeaveRequest relatedRequest = null;
        if (relatedRequestId != null) {
            relatedRequest = leaveRequestRepository.findById(relatedRequestId).orElse(null);
        }

        Notification notification = new Notification();
        notification.setUser(recipient);
        notification.setMessage(message);
        notification.setRead(false);
        notification.setRelatedRequest(relatedRequest);
        notification.setCreatedAt(LocalDateTime.now());
        notificationRepository.save(notification);

        EmailOutbox email = new EmailOutbox();
        email.setToEmail(recipient.getEmail());
        email.setSubject(emailSubject);
        email.setBody(emailBody != null ? emailBody : message);
        email.setSent(true);
        email.setCreatedAt(LocalDateTime.now());
        emailOutboxRepository.save(email);
    }

    @Transactional(readOnly = true)
    public List<NotificationDto> getMyNotifications(User user) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(n -> new NotificationDto(
                        n.getId(),
                        n.getMessage(),
                        n.isRead(),
                        n.getCreatedAt(),
                        n.getRelatedRequest() != null ? n.getRelatedRequest().getId() : null
                )).toList();
    }

    @Transactional
    public void markAsRead(User user, Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found: " + id));

        if (!notification.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You can only acknowledge your own notifications");
        }

        notification.setRead(true);
        notificationRepository.save(notification);
    }
}
