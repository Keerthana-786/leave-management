package com.hackathon.leave.config;

import com.hackathon.leave.model.*;
import com.hackathon.leave.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class DataLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);

    private final PasswordEncoder passwordEncoder;
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final ApprovalStepRepository approvalStepRepository;
    private final DelegationRepository delegationRepository;
    private final HolidayRepository holidayRepository;
    private final AuditEventRepository auditEventRepository;
    private final NotificationRepository notificationRepository;

    public DataLoader(
            PasswordEncoder passwordEncoder,
            TeamRepository teamRepository,
            UserRepository userRepository,
            LeaveTypeRepository leaveTypeRepository,
            LeaveBalanceRepository leaveBalanceRepository,
            LeaveRequestRepository leaveRequestRepository,
            ApprovalStepRepository approvalStepRepository,
            DelegationRepository delegationRepository,
            HolidayRepository holidayRepository,
            AuditEventRepository auditEventRepository,
            NotificationRepository notificationRepository
    ) {
        this.passwordEncoder = passwordEncoder;
        this.teamRepository = teamRepository;
        this.userRepository = userRepository;
        this.leaveTypeRepository = leaveTypeRepository;
        this.leaveBalanceRepository = leaveBalanceRepository;
        this.leaveRequestRepository = leaveRequestRepository;
        this.approvalStepRepository = approvalStepRepository;
        this.delegationRepository = delegationRepository;
        this.holidayRepository = holidayRepository;
        this.auditEventRepository = auditEventRepository;
        this.notificationRepository = notificationRepository;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Database already initialized. Skipping seed.");
            return;
        }

        log.info("Executing BE-2 Seed Data initialization...");
        String sharedPasswordHash = passwordEncoder.encode("Demo@123");

        // 1. Teams
        Team teamEng = teamRepository.save(new Team(null, "Engineering"));
        Team teamProd = teamRepository.save(new Team(null, "Product"));

        // 2. HR Admin
        User hr = userRepository.save(User.builder()
                .name("Hema HR")
                .email("hr@co.com")
                .passwordHash(sharedPasswordHash)
                .role(Role.HR)
                .team(teamEng)
                .manager(null)
                .joinDate(LocalDate.of(2018, 1, 1))
                .active(true)
                .build());

        // 3. Managers
        User manoj = userRepository.save(User.builder()
                .name("Manoj Kumar")
                .email("manager1@co.com")
                .passwordHash(sharedPasswordHash)
                .role(Role.MANAGER)
                .team(teamEng)
                .manager(hr)
                .joinDate(LocalDate.of(2020, 1, 1))
                .active(true)
                .build());

        User priya = userRepository.save(User.builder()
                .name("Priya Sharma")
                .email("manager2@co.com")
                .passwordHash(sharedPasswordHash)
                .role(Role.MANAGER)
                .team(teamProd)
                .manager(hr)
                .joinDate(LocalDate.of(2019, 6, 1))
                .active(true)
                .build());

        // 4. Employees (12)
        // Team Engineering:
        User riya = userRepository.save(User.builder()
                .name("Riya Sen")
                .email("riya@co.com")
                .passwordHash(sharedPasswordHash)
                .role(Role.EMPLOYEE)
                .team(teamEng)
                .manager(manoj)
                .joinDate(LocalDate.of(2026, 7, 1)) // Mid-year joiner (6 months)
                .active(true)
                .build());

        User arun = userRepository.save(User.builder()
                .name("Arun Patel")
                .email("arun@co.com")
                .passwordHash(sharedPasswordHash)
                .role(Role.EMPLOYEE)
                .team(teamEng)
                .manager(manoj)
                .joinDate(LocalDate.of(2024, 3, 1))
                .active(true)
                .build());

        User sam = userRepository.save(User.builder()
                .name("Sam Wilson")
                .email("sam@co.com")
                .passwordHash(sharedPasswordHash)
                .role(Role.EMPLOYEE)
                .team(teamEng)
                .manager(manoj)
                .joinDate(LocalDate.of(2023, 6, 1))
                .active(true)
                .build());

        User karan = userRepository.save(User.builder()
                .name("Karan Mehta")
                .email("karan@co.com")
                .passwordHash(sharedPasswordHash)
                .role(Role.EMPLOYEE)
                .team(teamEng)
                .manager(manoj)
                .joinDate(LocalDate.of(2021, 11, 1))
                .active(true)
                .build());

        User deepak = userRepository.save(User.builder()
                .name("Deepak Verma")
                .email("deepak@co.com")
                .passwordHash(sharedPasswordHash)
                .role(Role.EMPLOYEE)
                .team(teamEng)
                .manager(manoj)
                .joinDate(LocalDate.of(2022, 1, 15))
                .active(true)
                .build());

        User neha = userRepository.save(User.builder()
                .name("Neha Gupta")
                .email("neha@co.com")
                .passwordHash(sharedPasswordHash)
                .role(Role.EMPLOYEE)
                .team(teamEng)
                .manager(manoj)
                .joinDate(LocalDate.of(2025, 5, 10))
                .active(true)
                .build());

        // Team Product:
        User vikram = userRepository.save(User.builder()
                .name("Vikram Rao")
                .email("vikram@co.com")
                .passwordHash(sharedPasswordHash)
                .role(Role.EMPLOYEE)
                .team(teamProd)
                .manager(priya)
                .joinDate(LocalDate.of(2023, 4, 1))
                .active(true)
                .build());

        User ananya = userRepository.save(User.builder()
                .name("Ananya Roy")
                .email("ananya@co.com")
                .passwordHash(sharedPasswordHash)
                .role(Role.EMPLOYEE)
                .team(teamProd)
                .manager(priya)
                .joinDate(LocalDate.of(2024, 8, 15))
                .active(true)
                .build());

        User rohit = userRepository.save(User.builder()
                .name("Rohit Joshi")
                .email("rohit@co.com")
                .passwordHash(sharedPasswordHash)
                .role(Role.EMPLOYEE)
                .team(teamProd)
                .manager(priya)
                .joinDate(LocalDate.of(2022, 2, 1))
                .active(true)
                .build());

        User sneha = userRepository.save(User.builder()
                .name("Sneha Nair")
                .email("sneha@co.com")
                .passwordHash(sharedPasswordHash)
                .role(Role.EMPLOYEE)
                .team(teamProd)
                .manager(priya)
                .joinDate(LocalDate.of(2025, 9, 1))
                .active(true)
                .build());

        User tarun = userRepository.save(User.builder()
                .name("Tarun Sethi")
                .email("tarun@co.com")
                .passwordHash(sharedPasswordHash)
                .role(Role.EMPLOYEE)
                .team(teamProd)
                .manager(priya)
                .joinDate(LocalDate.of(2024, 11, 1))
                .active(true)
                .build());

        User kavita = userRepository.save(User.builder()
                .name("Kavita Das")
                .email("kavita@co.com")
                .passwordHash(sharedPasswordHash)
                .role(Role.EMPLOYEE)
                .team(teamProd)
                .manager(priya)
                .joinDate(LocalDate.of(2023, 10, 10))
                .active(true)
                .build());

        List<User> allUsers = List.of(hr, manoj, priya, riya, arun, sam, karan, deepak, neha, vikram, ananya, rohit, sneha, tarun, kavita);

        // 5. Leave Types
        LeaveType annual = leaveTypeRepository.save(new LeaveType(null, "ANNUAL", "Annual Leave", 24, true, false, true));
        LeaveType sick = leaveTypeRepository.save(new LeaveType(null, "SICK", "Sick Leave", 12, false, false, true));
        LeaveType casual = leaveTypeRepository.save(new LeaveType(null, "CASUAL", "Casual Leave", 8, false, false, true));
        LeaveType unpaid = leaveTypeRepository.save(new LeaveType(null, "UNPAID", "Unpaid Leave", 0, false, true, false));

        // 6. Leave Balances for 2026
        int currentYear = 2026;
        for (User u : allUsers) {
            double annualEntitled = u.getId().equals(riya.getId()) ? 12.0 : 24.0;
            leaveBalanceRepository.save(new LeaveBalance(null, u, annual, currentYear, annualEntitled, 0.0, 0.0));
            leaveBalanceRepository.save(new LeaveBalance(null, u, sick, currentYear, 12.0, 0.0, 0.0));
            leaveBalanceRepository.save(new LeaveBalance(null, u, casual, currentYear, 8.0, 0.0, 0.0));
        }

        // 7. Indian Public Holidays (Oct - Dec 2026)
        holidayRepository.save(new Holiday(null, LocalDate.of(2026, 10, 2), "Gandhi Jayanti"));
        holidayRepository.save(new Holiday(null, LocalDate.of(2026, 10, 20), "Dussehra"));
        holidayRepository.save(new Holiday(null, LocalDate.of(2026, 11, 9), "Diwali"));
        holidayRepository.save(new Holiday(null, LocalDate.of(2026, 11, 10), "Govardhan Puja"));
        holidayRepository.save(new Holiday(null, LocalDate.of(2026, 12, 25), "Christmas"));

        // 8. Active Delegation: Priya (manager2) delegates to Vikram Rao in late Oct 2026
        Delegation priyaDelegation = delegationRepository.save(Delegation.builder()
                .delegator(priya)
                .delegate(vikram)
                .fromDate(LocalDate.of(2026, 10, 20))
                .toDate(LocalDate.of(2026, 10, 31))
                .active(true)
                .build());

        // 9. Leave Requests in various states
        // Req 1: Arun - APPROVED (Annual, Oct 5-7, 3 days)
        LeaveRequest req1 = leaveRequestRepository.save(LeaveRequest.builder()
                .employee(arun)
                .leaveType(annual)
                .fromDate(LocalDate.of(2026, 10, 5))
                .toDate(LocalDate.of(2026, 10, 7))
                .days(3)
                .reason("Autumn family trip")
                .status(LeaveStatus.APPROVED)
                .flagged(false)
                .createdAt(LocalDateTime.of(2026, 9, 15, 10, 0))
                .lastActionAt(LocalDateTime.of(2026, 9, 16, 11, 0))
                .build());
        updateUserBalance(arun.getId(), annual.getId(), currentYear, 3.0, true);
        createApprovalStep(req1, ApprovalStage.MANAGER, manoj, null, ApprovalDecision.APPROVED, "Approved by Manoj");
        createAuditEvent(req1, arun, "APPLY", null, LeaveStatus.PENDING_MANAGER, "Applied");
        createAuditEvent(req1, manoj, "APPROVE", LeaveStatus.PENDING_MANAGER, LeaveStatus.APPROVED, "Approved by Manoj");

        // Req 2: Sam - APPROVED (Annual, Oct 5-7, 3 days - concurrent with Arun)
        LeaveRequest req2 = leaveRequestRepository.save(LeaveRequest.builder()
                .employee(sam)
                .leaveType(annual)
                .fromDate(LocalDate.of(2026, 10, 5))
                .toDate(LocalDate.of(2026, 10, 7))
                .days(3)
                .reason("Personal work")
                .status(LeaveStatus.APPROVED)
                .flagged(false)
                .createdAt(LocalDateTime.of(2026, 9, 17, 9, 0))
                .lastActionAt(LocalDateTime.of(2026, 9, 18, 14, 0))
                .build());
        updateUserBalance(sam.getId(), annual.getId(), currentYear, 3.0, true);
        createApprovalStep(req2, ApprovalStage.MANAGER, manoj, null, ApprovalDecision.APPROVED, "Approved by Manoj");
        createAuditEvent(req2, sam, "APPLY", null, LeaveStatus.PENDING_MANAGER, "Applied");
        createAuditEvent(req2, manoj, "APPROVE", LeaveStatus.PENDING_MANAGER, LeaveStatus.APPROVED, "Approved by Manoj");

        // Req 3: Karan - PENDING_MANAGER (Flagged: conflict with Arun and Sam on Oct 5-7)
        LeaveRequest req3 = leaveRequestRepository.save(LeaveRequest.builder()
                .employee(karan)
                .leaveType(annual)
                .fromDate(LocalDate.of(2026, 10, 5))
                .toDate(LocalDate.of(2026, 10, 7))
                .days(3)
                .reason("Festival celebration")
                .status(LeaveStatus.PENDING_MANAGER)
                .flagged(true)
                .flagReason("Headcount alert: 3 of 7 team members on leave: Arun Patel, Sam Wilson, Karan Mehta (42.9% > 30.0%)")
                .createdAt(LocalDateTime.of(2026, 9, 20, 10, 0))
                .lastActionAt(LocalDateTime.of(2026, 9, 20, 10, 0))
                .build());
        reservePendingBalance(karan.getId(), annual.getId(), currentYear, 3.0);
        createApprovalStep(req3, ApprovalStage.MANAGER, manoj, null, ApprovalDecision.PENDING, null);
        createAuditEvent(req3, karan, "APPLY", null, LeaveStatus.PENDING_MANAGER, "Applied with conflict flag");

        // Req 4: Deepak - APPROVED (Sick, Oct 12-14, 3 days)
        LeaveRequest req4 = leaveRequestRepository.save(LeaveRequest.builder()
                .employee(deepak)
                .leaveType(sick)
                .fromDate(LocalDate.of(2026, 10, 12))
                .toDate(LocalDate.of(2026, 10, 14))
                .days(3)
                .reason("Medical recovery")
                .status(LeaveStatus.APPROVED)
                .flagged(false)
                .createdAt(LocalDateTime.of(2026, 9, 21, 8, 30))
                .lastActionAt(LocalDateTime.of(2026, 9, 21, 16, 0))
                .build());
        updateUserBalance(deepak.getId(), sick.getId(), currentYear, 3.0, true);
        createApprovalStep(req4, ApprovalStage.MANAGER, manoj, null, ApprovalDecision.APPROVED, "Take care and recover");
        createAuditEvent(req4, deepak, "APPLY", null, LeaveStatus.PENDING_MANAGER, "Medical leave");
        createAuditEvent(req4, manoj, "APPROVE", LeaveStatus.PENDING_MANAGER, LeaveStatus.APPROVED, "Approved by Manoj");

        // Req 5: Neha - PENDING_MANAGER (Casual, Oct 21-23, 3 days)
        LeaveRequest req5 = leaveRequestRepository.save(LeaveRequest.builder()
                .employee(neha)
                .leaveType(casual)
                .fromDate(LocalDate.of(2026, 10, 21))
                .toDate(LocalDate.of(2026, 10, 23))
                .days(3)
                .reason("Family function")
                .status(LeaveStatus.PENDING_MANAGER)
                .flagged(false)
                .createdAt(LocalDateTime.of(2026, 9, 25, 11, 0))
                .lastActionAt(LocalDateTime.of(2026, 9, 25, 11, 0))
                .build());
        reservePendingBalance(neha.getId(), casual.getId(), currentYear, 3.0);
        createApprovalStep(req5, ApprovalStage.MANAGER, manoj, null, ApprovalDecision.PENDING, null);
        createAuditEvent(req5, neha, "APPLY", null, LeaveStatus.PENDING_MANAGER, "Applied");

        // Req 6: Riya - PENDING_HR (Unpaid, Nov 16-20, 5 days - manager approved, awaits HR)
        LeaveRequest req6 = leaveRequestRepository.save(LeaveRequest.builder()
                .employee(riya)
                .leaveType(unpaid)
                .fromDate(LocalDate.of(2026, 11, 16))
                .toDate(LocalDate.of(2026, 11, 20))
                .days(5)
                .reason("Extended personal sabbatical")
                .status(LeaveStatus.PENDING_HR)
                .flagged(false)
                .createdAt(LocalDateTime.of(2026, 9, 22, 10, 0))
                .lastActionAt(LocalDateTime.of(2026, 9, 23, 15, 0))
                .build());
        createApprovalStep(req6, ApprovalStage.MANAGER, manoj, null, ApprovalDecision.APPROVED, "Manager approved; forwarding to HR for unpaid leave review");
        createApprovalStep(req6, ApprovalStage.HR, hr, null, ApprovalDecision.PENDING, null);
        createAuditEvent(req6, riya, "APPLY", null, LeaveStatus.PENDING_MANAGER, "Applied for unpaid leave");
        createAuditEvent(req6, manoj, "APPROVE_MANAGER", LeaveStatus.PENDING_MANAGER, LeaveStatus.PENDING_HR, "Manager approved, awaits HR");

        // Req 7: Ananya - ESCALATED (Annual, Oct 14-16, 3 days - timeout SLA expired)
        LeaveRequest req7 = leaveRequestRepository.save(LeaveRequest.builder()
                .employee(ananya)
                .leaveType(annual)
                .fromDate(LocalDate.of(2026, 10, 14))
                .toDate(LocalDate.of(2026, 10, 16))
                .days(3)
                .reason("Short vacation")
                .status(LeaveStatus.ESCALATED)
                .flagged(false)
                .createdAt(LocalDateTime.of(2026, 9, 10, 9, 0))
                .lastActionAt(LocalDateTime.of(2026, 9, 10, 9, 2))
                .build());
        reservePendingBalance(ananya.getId(), annual.getId(), currentYear, 3.0);
        createApprovalStep(req7, ApprovalStage.MANAGER, priya, null, ApprovalDecision.ESCALATED, "Auto-escalated to HR due to review timeout");
        createApprovalStep(req7, ApprovalStage.HR, hr, null, ApprovalDecision.PENDING, null);
        createAuditEvent(req7, ananya, "APPLY", null, LeaveStatus.PENDING_MANAGER, "Applied");
        createAuditEvent(req7, null, "ESCALATE_TIMEOUT", LeaveStatus.PENDING_MANAGER, LeaveStatus.ESCALATED, "SYSTEM: Review timeout exceeded");

        // Req 8: Rohit - REJECTED (Annual, Oct 8-9, 2 days)
        LeaveRequest req8 = leaveRequestRepository.save(LeaveRequest.builder()
                .employee(rohit)
                .leaveType(annual)
                .fromDate(LocalDate.of(2026, 10, 8))
                .toDate(LocalDate.of(2026, 10, 9))
                .days(2)
                .reason("Long weekend")
                .status(LeaveStatus.REJECTED)
                .flagged(false)
                .createdAt(LocalDateTime.of(2026, 9, 18, 14, 0))
                .lastActionAt(LocalDateTime.of(2026, 9, 19, 10, 0))
                .build());
        createApprovalStep(req8, ApprovalStage.MANAGER, priya, null, ApprovalDecision.REJECTED, "Critical client release milestone; cannot approve");
        createAuditEvent(req8, rohit, "APPLY", null, LeaveStatus.PENDING_MANAGER, "Applied");
        createAuditEvent(req8, priya, "REJECT", LeaveStatus.PENDING_MANAGER, LeaveStatus.REJECTED, "Critical client release milestone; cannot approve");

        // Req 9: Sneha - CANCELLED (Casual, Oct 12-13, 2 days)
        LeaveRequest req9 = leaveRequestRepository.save(LeaveRequest.builder()
                .employee(sneha)
                .leaveType(casual)
                .fromDate(LocalDate.of(2026, 10, 12))
                .toDate(LocalDate.of(2026, 10, 13))
                .days(2)
                .reason("Dentist appointment")
                .status(LeaveStatus.CANCELLED)
                .flagged(false)
                .createdAt(LocalDateTime.of(2026, 9, 19, 12, 0))
                .lastActionAt(LocalDateTime.of(2026, 9, 20, 8, 0))
                .build());
        createAuditEvent(req9, sneha, "APPLY", null, LeaveStatus.PENDING_MANAGER, "Applied");
        createAuditEvent(req9, sneha, "CANCEL", LeaveStatus.PENDING_MANAGER, LeaveStatus.CANCELLED, "Plans postponed");

        // Req 10: Tarun - DELEGATED (Annual, Oct 26-28, 3 days - assigned to Vikram on behalf of Priya)
        LeaveRequest req10 = leaveRequestRepository.save(LeaveRequest.builder()
                .employee(tarun)
                .leaveType(annual)
                .fromDate(LocalDate.of(2026, 10, 26))
                .toDate(LocalDate.of(2026, 10, 28))
                .days(3)
                .reason("House shifting")
                .status(LeaveStatus.PENDING_MANAGER)
                .flagged(false)
                .createdAt(LocalDateTime.of(2026, 9, 24, 15, 0))
                .lastActionAt(LocalDateTime.of(2026, 9, 24, 15, 0))
                .build());
        reservePendingBalance(tarun.getId(), annual.getId(), currentYear, 3.0);
        createApprovalStep(req10, ApprovalStage.MANAGER, vikram, priya, ApprovalDecision.PENDING, null);
        createAuditEvent(req10, tarun, "APPLY", null, LeaveStatus.PENDING_MANAGER, "Applied (routed to delegate Vikram Rao)");

        // 10. Sample Notifications
        notificationRepository.save(Notification.builder()
                .user(manoj)
                .message("New leave request pending your review from Karan Mehta (Flagged: Team capacity warning)")
                .read(false)
                .createdAt(LocalDateTime.of(2026, 9, 20, 10, 1))
                .relatedRequest(req3)
                .build());

        notificationRepository.save(Notification.builder()
                .user(hr)
                .message("Leave request for Ananya Roy auto-escalated to HR due to manager timeout")
                .read(false)
                .createdAt(LocalDateTime.of(2026, 9, 10, 9, 3))
                .relatedRequest(req7)
                .build());

        log.info("BE-2 Seed Data successfully loaded: 2 teams, 15 users, 4 leave types, 5 holidays, 1 delegation, and 10 representative leave requests.");
    }

    private void updateUserBalance(Long userId, Long leaveTypeId, int year, double days, boolean isApproved) {
        leaveBalanceRepository.findByUserIdAndLeaveTypeIdAndYear(userId, leaveTypeId, year).ifPresent(b -> {
            if (isApproved) {
                b.setUsed(b.getUsed() + days);
            }
            leaveBalanceRepository.save(b);
        });
    }

    private void reservePendingBalance(Long userId, Long leaveTypeId, int year, double days) {
        leaveBalanceRepository.findByUserIdAndLeaveTypeIdAndYear(userId, leaveTypeId, year).ifPresent(b -> {
            b.setPending(b.getPending() + days);
            leaveBalanceRepository.save(b);
        });
    }

    private void createApprovalStep(LeaveRequest req, ApprovalStage stage, User assignee, User delegatedFrom, ApprovalDecision decision, String comment) {
        approvalStepRepository.save(ApprovalStep.builder()
                .request(req)
                .stage(stage)
                .assignee(assignee)
                .delegatedFrom(delegatedFrom)
                .dueAt(LocalDateTime.now().plusMinutes(1))
                .decision(decision)
                .decidedAt(decision == ApprovalDecision.PENDING ? null : LocalDateTime.now())
                .comment(comment)
                .build());
    }

    private void createAuditEvent(LeaveRequest req, User actor, String action, LeaveStatus from, LeaveStatus to, String comment) {
        auditEventRepository.save(AuditEvent.builder()
                .request(req)
                .actor(actor)
                .action(action)
                .fromStatus(from)
                .toStatus(to)
                .comment(comment)
                .at(LocalDateTime.now())
                .build());
    }
}
