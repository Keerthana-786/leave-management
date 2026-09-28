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

import java.time.LocalDate;

@Entity
@Table(name = "delegations")
public class Delegation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delegator_id", nullable = false)
    private User delegator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delegate_id", nullable = false)
    private User delegate;

    @Column(name = "from_date", nullable = false)
    private LocalDate fromDate;

    @Column(name = "to_date", nullable = false)
    private LocalDate toDate;

    @Column(nullable = false)
    private boolean active = true;

    public Delegation() {
    }

    public Delegation(Long id, User delegator, User delegate, LocalDate fromDate, LocalDate toDate, boolean active) {
        this.id = id;
        this.delegator = delegator;
        this.delegate = delegate;
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.active = active;
    }

    public static DelegationBuilder builder() {
        return new DelegationBuilder();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getDelegator() {
        return delegator;
    }

    public void setDelegator(User delegator) {
        this.delegator = delegator;
    }

    public User getDelegate() {
        return delegate;
    }

    public void setDelegate(User delegate) {
        this.delegate = delegate;
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public static class DelegationBuilder {
        private Long id;
        private User delegator;
        private User delegate;
        private LocalDate fromDate;
        private LocalDate toDate;
        private boolean active = true;

        public DelegationBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public DelegationBuilder delegator(User delegator) {
            this.delegator = delegator;
            return this;
        }

        public DelegationBuilder delegate(User delegate) {
            this.delegate = delegate;
            return this;
        }

        public DelegationBuilder fromDate(LocalDate fromDate) {
            this.fromDate = fromDate;
            return this;
        }

        public DelegationBuilder toDate(LocalDate toDate) {
            this.toDate = toDate;
            return this;
        }

        public DelegationBuilder active(boolean active) {
            this.active = active;
            return this;
        }

        public Delegation build() {
            return new Delegation(id, delegator, delegate, fromDate, toDate, active);
        }
    }
}
