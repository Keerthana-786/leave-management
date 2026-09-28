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
@Table(name = "approval_steps")
public class ApprovalStep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    private LeaveRequest request;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApprovalStage stage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id", nullable = false)
    private User assignee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delegated_from_id")
    private User delegatedFrom;

    @Column(name = "due_at")
    private LocalDateTime dueAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApprovalDecision decision;

    @Column(name = "decided_at")
    private LocalDateTime decidedAt;

    private String comment;

    public ApprovalStep() {
    }

    public ApprovalStep(Long id, LeaveRequest request, ApprovalStage stage, User assignee, User delegatedFrom,
                        LocalDateTime dueAt, ApprovalDecision decision, LocalDateTime decidedAt, String comment) {
        this.id = id;
        this.request = request;
        this.stage = stage;
        this.assignee = assignee;
        this.delegatedFrom = delegatedFrom;
        this.dueAt = dueAt;
        this.decision = decision;
        this.decidedAt = decidedAt;
        this.comment = comment;
    }

    public static ApprovalStepBuilder builder() {
        return new ApprovalStepBuilder();
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

    public ApprovalStage getStage() {
        return stage;
    }

    public void setStage(ApprovalStage stage) {
        this.stage = stage;
    }

    public User getAssignee() {
        return assignee;
    }

    public void setAssignee(User assignee) {
        this.assignee = assignee;
    }

    public User getDelegatedFrom() {
        return delegatedFrom;
    }

    public void setDelegatedFrom(User delegatedFrom) {
        this.delegatedFrom = delegatedFrom;
    }

    public LocalDateTime getDueAt() {
        return dueAt;
    }

    public void setDueAt(LocalDateTime dueAt) {
        this.dueAt = dueAt;
    }

    public ApprovalDecision getDecision() {
        return decision;
    }

    public void setDecision(ApprovalDecision decision) {
        this.decision = decision;
    }

    public LocalDateTime getDecidedAt() {
        return decidedAt;
    }

    public void setDecidedAt(LocalDateTime decidedAt) {
        this.decidedAt = decidedAt;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public static class ApprovalStepBuilder {
        private Long id;
        private LeaveRequest request;
        private ApprovalStage stage;
        private User assignee;
        private User delegatedFrom;
        private LocalDateTime dueAt;
        private ApprovalDecision decision;
        private LocalDateTime decidedAt;
        private String comment;

        public ApprovalStepBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public ApprovalStepBuilder request(LeaveRequest request) {
            this.request = request;
            return this;
        }

        public ApprovalStepBuilder stage(ApprovalStage stage) {
            this.stage = stage;
            return this;
        }

        public ApprovalStepBuilder assignee(User assignee) {
            this.assignee = assignee;
            return this;
        }

        public ApprovalStepBuilder delegatedFrom(User delegatedFrom) {
            this.delegatedFrom = delegatedFrom;
            return this;
        }

        public ApprovalStepBuilder dueAt(LocalDateTime dueAt) {
            this.dueAt = dueAt;
            return this;
        }

        public ApprovalStepBuilder decision(ApprovalDecision decision) {
            this.decision = decision;
            return this;
        }

        public ApprovalStepBuilder decidedAt(LocalDateTime decidedAt) {
            this.decidedAt = decidedAt;
            return this;
        }

        public ApprovalStepBuilder comment(String comment) {
            this.comment = comment;
            return this;
        }

        public ApprovalStep build() {
            return new ApprovalStep(id, request, stage, assignee, delegatedFrom, dueAt, decision, decidedAt, comment);
        }
    }
}
