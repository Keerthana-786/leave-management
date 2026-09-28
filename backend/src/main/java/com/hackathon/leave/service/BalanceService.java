package com.hackathon.leave.service;

import com.hackathon.leave.dto.LeaveBalanceDto;
import com.hackathon.leave.model.LeaveBalance;
import com.hackathon.leave.model.User;
import com.hackathon.leave.repository.LeaveBalanceRepository;
import com.hackathon.leave.service.rule.ProRataPolicy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class BalanceService {

    private final LeaveBalanceRepository leaveBalanceRepository;
    private final ProRataPolicy proRataPolicy;

    public BalanceService(LeaveBalanceRepository leaveBalanceRepository, ProRataPolicy proRataPolicy) {
        this.leaveBalanceRepository = leaveBalanceRepository;
        this.proRataPolicy = proRataPolicy;
    }

    @Transactional(readOnly = true)
    public List<LeaveBalanceDto> getMyBalances(User employee) {
        int year = LocalDate.now().getYear();
        List<LeaveBalance> balances = leaveBalanceRepository.findByUserIdAndYear(employee.getId(), year);

        // If no balances found for current year, fallback to 2026 (seed year)
        if (balances.isEmpty() && year != 2026) {
            balances = leaveBalanceRepository.findByUserIdAndYear(employee.getId(), 2026);
        }

        return balances.stream().map(b -> {
            double remaining = b.getEntitled() - b.getUsed() - b.getPending();
            double available = remaining;
            String proRataExplanation = null;

            if ("ANNUAL".equalsIgnoreCase(b.getLeaveType().getCode())) {
                proRataExplanation = proRataPolicy.calculate(
                        employee.getJoinDate(), b.getYear()
                ).explanation();
            }

            return new LeaveBalanceDto(
                    b.getId(),
                    b.getLeaveType().getId(),
                    b.getLeaveType().getCode(),
                    b.getLeaveType().getName(),
                    b.getYear(),
                    b.getEntitled(),
                    b.getUsed(),
                    b.getPending(),
                    remaining,
                    available,
                    proRataExplanation
            );
        }).toList();
    }
}
