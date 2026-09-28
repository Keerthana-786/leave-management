package com.hackathon.leave.service.rule;

import com.hackathon.leave.exception.BusinessRuleException;
import com.hackathon.leave.model.Holiday;
import com.hackathon.leave.repository.HolidayRepository;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class WorkingDayCalculator {

    private final HolidayRepository holidayRepository;

    public WorkingDayCalculator(HolidayRepository holidayRepository) {
        this.holidayRepository = holidayRepository;
    }

    public record WorkingDayResult(
            int workingDays,
            List<String> holidayDates
    ) {}

    public WorkingDayResult calculate(LocalDate fromDate, LocalDate toDate) {
        if (fromDate == null || toDate == null) {
            throw new BusinessRuleException("From date and to date are required");
        }
        if (fromDate.isAfter(toDate)) {
            throw new BusinessRuleException("From date must be on or before to date");
        }

        List<Holiday> holidays = holidayRepository.findByDateBetweenOrderByDateAsc(fromDate, toDate);
        Set<LocalDate> holidaySet = new HashSet<>();
        List<String> holidayDates = new ArrayList<>();
        for (Holiday h : holidays) {
            holidaySet.add(h.getDate());
            holidayDates.add(h.getDate().toString());
        }

        int workingDays = 0;
        LocalDate current = fromDate;
        while (!current.isAfter(toDate)) {
            DayOfWeek dow = current.getDayOfWeek();
            boolean isWeekend = (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY);
            boolean isHoliday = holidaySet.contains(current);

            if (!isWeekend && !isHoliday) {
                workingDays++;
            }
            current = current.plusDays(1);
        }

        if (workingDays == 0) {
            throw new BusinessRuleException("Selected date range contains no working days (all dates are weekends or public holidays)");
        }

        return new WorkingDayResult(workingDays, holidayDates);
    }
}
