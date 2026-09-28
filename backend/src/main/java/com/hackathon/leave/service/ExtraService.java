package com.hackathon.leave.service;

import com.hackathon.leave.dto.*;
import com.hackathon.leave.exception.BusinessRuleException;
import com.hackathon.leave.model.*;
import com.hackathon.leave.repository.*;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ExtraService {

    private final HolidayRepository holidayRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;

    public ExtraService(
            HolidayRepository holidayRepository,
            LeaveRequestRepository leaveRequestRepository,
            UserRepository userRepository,
            TeamRepository teamRepository
    ) {
        this.holidayRepository = holidayRepository;
        this.leaveRequestRepository = leaveRequestRepository;
        this.userRepository = userRepository;
        this.teamRepository = teamRepository;
    }

    @Transactional(readOnly = true)
    public List<HolidayDto> getHolidays() {
        return holidayRepository.findAll(Sort.by(Sort.Direction.ASC, "date")).stream()
                .map(h -> new HolidayDto(h.getId(), h.getDate(), h.getName()))
                .toList();
    }

    @Transactional
    public HolidayDto createHoliday(HolidayCreateRequest request) {
        if (holidayRepository.findByDate(request.date()).isPresent()) {
            throw new BusinessRuleException("A public holiday is already registered for date: " + request.date());
        }
        Holiday h = new Holiday(null, request.date(), request.name());
        Holiday saved = holidayRepository.save(h);
        return new HolidayDto(saved.getId(), saved.getDate(), saved.getName());
    }

    @Transactional(readOnly = true)
    public TeamCalendarDto getTeamCalendar(User user, String monthStr) {
        Team team = user.getTeam();
        if (team == null) {
            team = teamRepository.findAll().stream().findFirst().orElse(null);
        }
        Long teamId = team != null ? team.getId() : 1L;
        String teamName = team != null ? team.getName() : "All";

        YearMonth ym;
        try {
            ym = (monthStr != null && !monthStr.isBlank()) ? YearMonth.parse(monthStr) : YearMonth.of(2026, 10);
        } catch (Exception e) {
            ym = YearMonth.of(2026, 10);
        }

        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();

        List<User> teamMembers = team != null ? userRepository.findByTeamId(team.getId()) : userRepository.findAll();
        List<Long> memberIds = teamMembers.stream().map(User::getId).toList();

        List<LeaveRequest> requests = leaveRequestRepository.findAll().stream()
                .filter(r -> memberIds.contains(r.getEmployee().getId()))
                .filter(r -> !r.getFromDate().isAfter(end) && !r.getToDate().isBefore(start))
                .filter(r -> r.getStatus() != LeaveStatus.CANCELLED && r.getStatus() != LeaveStatus.REJECTED)
                .toList();

        List<TeamLeaveEventDto> events = requests.stream().map(r -> new TeamLeaveEventDto(
                r.getId(),
                r.getEmployee().getId(),
                r.getEmployee().getName(),
                r.getFromDate(),
                r.getToDate(),
                r.getStatus(),
                r.getLeaveType().getCode()
        )).toList();

        return new TeamCalendarDto(teamId, teamName, ym.toString(), events);
    }

    @Transactional(readOnly = true)
    public AnalyticsSummaryDto getAnalyticsSummary() {
        List<LeaveRequest> all = leaveRequestRepository.findAll();

        long total = all.size();
        long approved = all.stream().filter(r -> r.getStatus() == LeaveStatus.APPROVED).count();
        long pendingMgr = all.stream().filter(r -> r.getStatus() == LeaveStatus.PENDING_MANAGER).count();
        long pendingHr = all.stream().filter(r -> r.getStatus() == LeaveStatus.PENDING_HR).count();
        long escalated = all.stream().filter(r -> r.getStatus() == LeaveStatus.ESCALATED).count();
        long rejected = all.stream().filter(r -> r.getStatus() == LeaveStatus.REJECTED).count();
        long cancelled = all.stream().filter(r -> r.getStatus() == LeaveStatus.CANCELLED).count();

        double avgDays = all.isEmpty() ? 0.0 : all.stream().mapToInt(LeaveRequest::getDays).average().orElse(0.0);

        Map<String, Long> byType = all.stream()
                .collect(Collectors.groupingBy(r -> r.getLeaveType().getCode(), Collectors.counting()));

        return new AnalyticsSummaryDto(
                total,
                approved,
                pendingMgr,
                pendingHr,
                escalated,
                rejected,
                cancelled,
                Math.round(avgDays * 10.0) / 10.0,
                byType
        );
    }
}
