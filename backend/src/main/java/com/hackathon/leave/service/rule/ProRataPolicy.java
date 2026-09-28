package com.hackathon.leave.service.rule;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Component
public class ProRataPolicy {

    private final double defaultAnnualQuota;

    public ProRataPolicy(@Value("${leave.annual-quota-default:24}") double defaultAnnualQuota) {
        this.defaultAnnualQuota = defaultAnnualQuota;
    }

    public record ProRataResult(
            double proratedQuota,
            int monthsRemaining,
            String explanation
    ) {}

    public ProRataResult calculate(LocalDate joinDate, int targetYear) {
        return calculate(joinDate, defaultAnnualQuota, targetYear);
    }

    public ProRataResult calculate(LocalDate joinDate, double annualQuota, int targetYear) {
        int monthsRemaining = 12;

        if (joinDate != null) {
            if (joinDate.getYear() == targetYear) {
                // join month inclusive
                monthsRemaining = 12 - joinDate.getMonthValue() + 1;
                if (monthsRemaining < 0) {
                    monthsRemaining = 0;
                }
            } else if (joinDate.getYear() > targetYear) {
                monthsRemaining = 0;
            } else {
                monthsRemaining = 12;
            }
        }

        double raw = (annualQuota * monthsRemaining) / 12.0;
        BigDecimal bd = BigDecimal.valueOf(raw).setScale(1, RoundingMode.HALF_UP);
        double rounded = bd.doubleValue();

        String explanation;
        if (rounded == (long) rounded && annualQuota == (long) annualQuota) {
            explanation = String.format("%.0f x %d/12 = %.0f", annualQuota, monthsRemaining, rounded);
        } else {
            explanation = String.format("%.1f x %d/12 = %.1f", annualQuota, monthsRemaining, rounded);
        }

        return new ProRataResult(rounded, monthsRemaining, explanation);
    }

    public double getDefaultAnnualQuota() {
        return defaultAnnualQuota;
    }
}
