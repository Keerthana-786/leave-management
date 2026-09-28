package com.hackathon.leave.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String message;

    @Column(name = "is_read", nullable = false)
    private boolean read = false;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_request_id")
    private LeaveRequest relatedRequest;

    public Notification() {
    }

    public Notification(Long id, User user, String message, boolean read, LocalDateTime createdAt, LeaveRequest relatedRequest) {
        this.id = id;
        this.user = user;
        this.message = message;
        this.read = read;
        this.createdAt = createdAt;
        this.relatedRequest = relatedRequest;
    }

    public static NotificationBuilder builder() {
        return new NotificationBuilder();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LeaveRequest getRelatedRequest() {
        return relatedRequest;
    }

    public void setRelatedRequest(LeaveRequest relatedRequest) {
        this.relatedRequest = relatedRequest;
    }

    public static class NotificationBuilder {
        private Long id;
        private User user;
        private String message;
        private boolean read = false;
        private LocalDateTime createdAt;
        private LeaveRequest relatedRequest;

        public NotificationBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public NotificationBuilder user(User user) {
            this.user = user;
            return this;
        }

        public NotificationBuilder message(String message) {
            this.message = message;
            return this;
        }

        public NotificationBuilder read(boolean read) {
            this.read = read;
            return this;
        }

        public NotificationBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public NotificationBuilder relatedRequest(LeaveRequest relatedRequest) {
            this.relatedRequest = relatedRequest;
            return this;
        }

        public Notification build() {
            return new Notification(id, user, message, read, createdAt, relatedRequest);
        }
    }
}
