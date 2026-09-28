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
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    name = "leave_balances",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "leave_type_id", "balance_year"})
)
public class LeaveBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leave_type_id", nullable = false)
    private LeaveType leaveType;

    @Column(name = "balance_year", nullable = false)
    private int year;

    @Column(nullable = false)
    private double entitled;

    @Column(nullable = false)
    private double used;

    @Column(nullable = false)
    private double pending;

    public LeaveBalance() {
    }

    public LeaveBalance(Long id, User user, LeaveType leaveType, int year, double entitled, double used, double pending) {
        this.id = id;
        this.user = user;
        this.leaveType = leaveType;
        this.year = year;
        this.entitled = entitled;
        this.used = used;
        this.pending = pending;
    }

    public static LeaveBalanceBuilder builder() {
        return new LeaveBalanceBuilder();
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

    public LeaveType getLeaveType() {
        return leaveType;
    }

    public void setLeaveType(LeaveType leaveType) {
        this.leaveType = leaveType;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public double getEntitled() {
        return entitled;
    }

    public void setEntitled(double entitled) {
        this.entitled = entitled;
    }

    public double getUsed() {
        return used;
    }

    public void setUsed(double used) {
        this.used = used;
    }

    public double getPending() {
        return pending;
    }

    public void setPending(double pending) {
        this.pending = pending;
    }

    public static class LeaveBalanceBuilder {
        private Long id;
        private User user;
        private LeaveType leaveType;
        private int year;
        private double entitled;
        private double used;
        private double pending;

        public LeaveBalanceBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public LeaveBalanceBuilder user(User user) {
            this.user = user;
            return this;
        }

        public LeaveBalanceBuilder leaveType(LeaveType leaveType) {
            this.leaveType = leaveType;
            return this;
        }

        public LeaveBalanceBuilder year(int year) {
            this.year = year;
            return this;
        }

        public LeaveBalanceBuilder entitled(double entitled) {
            this.entitled = entitled;
            return this;
        }

        public LeaveBalanceBuilder used(double used) {
            this.used = used;
            return this;
        }

        public LeaveBalanceBuilder pending(double pending) {
            this.pending = pending;
            return this;
        }

        public LeaveBalance build() {
            return new LeaveBalance(id, user, leaveType, year, entitled, used, pending);
        }
    }
}
