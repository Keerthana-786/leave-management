package com.hackathon.leave.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_events")
public class AuditEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    private LeaveRequest request;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id")
    private User actor; // null means SYSTEM

    @Column(nullable = false)
    private String action;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_status")
    private LeaveStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_status")
    private LeaveStatus toStatus;

    private String comment;

    @Column(nullable = false)
    private LocalDateTime at;

    public AuditEvent() {
    }

    public AuditEvent(Long id, LeaveRequest request, User actor, String action, LeaveStatus fromStatus, LeaveStatus toStatus, String comment, LocalDateTime at) {
        this.id = id;
        this.request = request;
        this.actor = actor;
        this.action = action;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.comment = comment;
        this.at = at;
    }

    public static AuditEventBuilder builder() {
        return new AuditEventBuilder();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LeaveRequest getRequest() {
        return request;
    }

    public void setRequest(LeaveRequest request) {
        this.request = request;
    }

    public User getActor() {
        return actor;
    }

    public void setActor(User actor) {
        this.actor = actor;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public LeaveStatus getFromStatus() {
        return fromStatus;
    }

    public void setFromStatus(LeaveStatus fromStatus) {
        this.fromStatus = fromStatus;
    }

    public LeaveStatus getToStatus() {
        return toStatus;
    }

    public void setToStatus(LeaveStatus toStatus) {
        this.toStatus = toStatus;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LocalDateTime getAt() {
        return at;
    }

    public void setAt(LocalDateTime at) {
        this.at = at;
    }

    public static class AuditEventBuilder {
        private Long id;
        private LeaveRequest request;
        private User actor;
        private String action;
        private LeaveStatus fromStatus;
        private LeaveStatus toStatus;
        private String comment;
        private LocalDateTime at;

        public AuditEventBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public AuditEventBuilder request(LeaveRequest request) {
            this.request = request;
            return this;
        }

        public AuditEventBuilder actor(User actor) {
            this.actor = actor;
            return this;
        }

        public AuditEventBuilder action(String action) {
            this.action = action;
            return this;
        }

        public AuditEventBuilder fromStatus(LeaveStatus fromStatus) {
            this.fromStatus = fromStatus;
            return this;
        }

        public AuditEventBuilder toStatus(LeaveStatus toStatus) {
            this.toStatus = toStatus;
            return this;
        }

        public AuditEventBuilder comment(String comment) {
            this.comment = comment;
            return this;
        }

        public AuditEventBuilder at(LocalDateTime at) {
            this.at = at;
            return this;
        }

        public AuditEvent build() {
            return new AuditEvent(id, request, actor, action, fromStatus, toStatus, comment, at);
        }
    }
}
