package com.hackathon.leave.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "leave_types")
public class LeaveType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(name = "annual_quota", nullable = false)
    private int annualQuota;

    @Column(name = "pro_rata", nullable = false)
    private boolean proRata;

    @Column(name = "requires_hr", nullable = false)
    private boolean requiresHr;

    @Column(nullable = false)
    private boolean paid;

    public LeaveType() {
    }

    public LeaveType(Long id, String code, String name, int annualQuota, boolean proRata, boolean requiresHr, boolean paid) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.annualQuota = annualQuota;
        this.proRata = proRata;
        this.requiresHr = requiresHr;
        this.paid = paid;
    }

    public static LeaveTypeBuilder builder() {
        return new LeaveTypeBuilder();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAnnualQuota() {
        return annualQuota;
    }

    public void setAnnualQuota(int annualQuota) {
        this.annualQuota = annualQuota;
    }

    public boolean isProRata() {
        return proRata;
    }

    public void setProRata(boolean proRata) {
        this.proRata = proRata;
    }

    public boolean isRequiresHr() {
        return requiresHr;
    }

    public void setRequiresHr(boolean requiresHr) {
        this.requiresHr = requiresHr;
    }

    public boolean isPaid() {
        return paid;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }

    public static class LeaveTypeBuilder {
        private Long id;
        private String code;
        private String name;
        private int annualQuota;
        private boolean proRata;
        private boolean requiresHr;
        private boolean paid;

        public LeaveTypeBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public LeaveTypeBuilder code(String code) {
            this.code = code;
            return this;
        }

        public LeaveTypeBuilder name(String name) {
            this.name = name;
            return this;
        }

        public LeaveTypeBuilder annualQuota(int annualQuota) {
            this.annualQuota = annualQuota;
            return this;
        }

        public LeaveTypeBuilder proRata(boolean proRata) {
            this.proRata = proRata;
            return this;
        }

        public LeaveTypeBuilder requiresHr(boolean requiresHr) {
            this.requiresHr = requiresHr;
            return this;
        }

        public LeaveTypeBuilder paid(boolean paid) {
            this.paid = paid;
            return this;
        }

        public LeaveType build() {
            return new LeaveType(id, code, name, annualQuota, proRata, requiresHr, paid);
        }
    }
}
