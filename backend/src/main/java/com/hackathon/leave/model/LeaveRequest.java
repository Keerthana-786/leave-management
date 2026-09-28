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

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "leave_requests")
public class LeaveRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private User employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leave_type_id", nullable = false)
    private LeaveType leaveType;

    @Column(name = "from_date", nullable = false)
    private LocalDate fromDate;

    @Column(name = "to_date", nullable = false)
    private LocalDate toDate;

    @Column(nullable = false)
    private int days;

    @Column(nullable = false)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LeaveStatus status;

    @Column(nullable = false)
    private boolean flagged;

    @Column(name = "flag_reason")
    private String flagReason;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_action_at")
    private LocalDateTime lastActionAt;

    public LeaveRequest() {
    }

    public LeaveRequest(Long id, User employee, LeaveType leaveType, LocalDate fromDate, LocalDate toDate,
                        int days, String reason, LeaveStatus status, boolean flagged, String flagReason,
                        LocalDateTime createdAt, LocalDateTime lastActionAt) {
        this.id = id;
        this.employee = employee;
        this.leaveType = leaveType;
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.days = days;
        this.reason = reason;
        this.status = status;
        this.flagged = flagged;
        this.flagReason = flagReason;
        this.createdAt = createdAt;
        this.lastActionAt = lastActionAt;
    }

    public static LeaveRequestBuilder builder() {
        return new LeaveRequestBuilder();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getEmployee() {
        return employee;
    }

    public void setEmployee(User employee) {
        this.employee = employee;
    }

    public LeaveType getLeaveType() {
        return leaveType;
    }

    public void setLeaveType(LeaveType leaveType) {
        this.leaveType = leaveType;
    }

    public LocalDate getFromDate() {
        return fromDate;
    }

    public void setFromDate(LocalDate fromDate) {
        this.fromDate = fromDate;
    }

    public LocalDate getToDate() {
        return toDate;
    }

    public void setToDate(LocalDate toDate) {
        this.toDate = toDate;
    }

    public int getDays() {
        return days;
    }

    public void setDays(int days) {
        this.days = days;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LeaveStatus getStatus() {
        return status;
    }

    public void setStatus(LeaveStatus status) {
        this.status = status;
    }

    public boolean isFlagged() {
        return flagged;
    }

    public void setFlagged(boolean flagged) {
        this.flagged = flagged;
    }

    public String getFlagReason() {
        return flagReason;
    }

    public void setFlagReason(String flagReason) {
        this.flagReason = flagReason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getLastActionAt() {
        return lastActionAt;
    }

    public void setLastActionAt(LocalDateTime lastActionAt) {
        this.lastActionAt = lastActionAt;
    }

    public static class LeaveRequestBuilder {
        private Long id;
        private User employee;
        private LeaveType leaveType;
        private LocalDate fromDate;
        private LocalDate toDate;
        private int days;
        private String reason;
        private LeaveStatus status;
        private boolean flagged;
        private String flagReason;
        private LocalDateTime createdAt;
        private LocalDateTime lastActionAt;

        public LeaveRequestBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public LeaveRequestBuilder employee(User employee) {
            this.employee = employee;
            return this;
        }

        public LeaveRequestBuilder leaveType(LeaveType leaveType) {
            this.leaveType = leaveType;
            return this;
        }

        public LeaveRequestBuilder fromDate(LocalDate fromDate) {
            this.fromDate = fromDate;
            return this;
        }

        public LeaveRequestBuilder toDate(LocalDate toDate) {
            this.toDate = toDate;
            return this;
        }

        public LeaveRequestBuilder days(int days) {
            this.days = days;
            return this;
        }

        public LeaveRequestBuilder reason(String reason) {
            this.reason = reason;
            return this;
        }

        public LeaveRequestBuilder status(LeaveStatus status) {
            this.status = status;
            return this;
        }

        public LeaveRequestBuilder flagged(boolean flagged) {
            this.flagged = flagged;
            return this;
        }

        public LeaveRequestBuilder flagReason(String flagReason) {
            this.flagReason = flagReason;
            return this;
        }

        public LeaveRequestBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public LeaveRequestBuilder lastActionAt(LocalDateTime lastActionAt) {
            this.lastActionAt = lastActionAt;
            return this;
        }

        public LeaveRequest build() {
            return new LeaveRequest(id, employee, leaveType, fromDate, toDate, days, reason, status, flagged, flagReason, createdAt, lastActionAt);
        }
    }
}
