package com.hackathon.leave;

import com.hackathon.leave.dto.*;
import com.hackathon.leave.exception.BusinessRuleException;
import com.hackathon.leave.exception.ConflictException;
import com.hackathon.leave.model.*;
import com.hackathon.leave.repository.*;
import com.hackathon.leave.security.JwtService;
import com.hackathon.leave.service.*;
import com.hackathon.leave.service.rule.ConflictEvaluator;
import com.hackathon.leave.service.rule.ConflictResult;
import com.hackathon.leave.service.rule.ProRataPolicy;
import com.hackathon.leave.service.rule.WorkingDayCalculator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class LeaveSystemTests {

    @Autowired
    private WorkingDayCalculator workingDayCalculator;

    @Autowired
    private ProRataPolicy proRataPolicy;

    @Autowired
    private ConflictEvaluator conflictEvaluator;

    @Autowired
    private LeaveService leaveService;

    @Autowired
    private WorkflowService workflowService;

    @Autowired
    private EscalationService escalationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LeaveTypeRepository leaveTypeRepository;

    @Autowired
    private LeaveBalanceRepository leaveBalanceRepository;

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    @Autowired
    private ApprovalStepRepository approvalStepRepository;

    @Autowired
    private AuditEventRepository auditEventRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtService jwtService;

    @Test
    @DisplayName("1. Working days skip weekends and public holidays; all-holiday range throws 422")
    void testWorkingDaysSkipWeekendsAndHolidays() {
        // Nov 9 and Nov 10, 2026 are Diwali holidays in seed data.
        // Monday Nov 9 to Friday Nov 13: 5 weekdays minus 2 holidays = 3 working days.
        WorkingDayCalculator.WorkingDayResult result = workingDayCalculator.calculate(
                LocalDate.of(2026, 11, 9),
                LocalDate.of(2026, 11, 13)
        );
        assertEquals(3, result.workingDays());
        assertEquals(2, result.holidayDates().size());

        // Range of only Saturday and Sunday -> throws BusinessRuleException (422)
        assertThrows(BusinessRuleException.class, () ->
                workingDayCalculator.calculate(LocalDate.of(2026, 10, 3), LocalDate.of(2026, 10, 4))
        );
    }

    @Test
    @DisplayName("2. Pro-rata policy: 24 x 6/12 = 12 for July 1 join date")
    void testProRataCalculation() {
        LocalDate joinDate = LocalDate.of(2026, 7, 1);
        ProRataPolicy.ProRataResult result = proRataPolicy.calculate(joinDate, 2026);
        assertEquals(12.0, result.proratedQuota());
        assertEquals(6, result.monthsRemaining());
        assertEquals("24 x 6/12 = 12", result.explanation());
    }

    @Test
    @DisplayName("3. Conflict evaluation flags but NEVER throws or rejects")
    void testConflictFlagsNotRejects() {
        User riya = userRepository.findByEmail("riya@co.com").orElseThrow();
        // Evaluating conflict during high-overlap period
        ConflictResult conflict = conflictEvaluator.evaluate(riya, LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 5), null);
        assertNotNull(conflict);
        // Even if flagged, it is an advisory flag, never a thrown exception
        assertDoesNotThrow(() -> {
            boolean isFlagged = conflict.flagged();
            assertNotNull(conflict.severity());
        });
    }

    @Test
    @DisplayName("4. Preview writes nothing to database")
    void testPreviewWritesNothing() {
        User riya = userRepository.findByEmail("riya@co.com").orElseThrow();
        long reqCountBefore = leaveRequestRepository.count();
        long stepCountBefore = approvalStepRepository.count();
        long auditCountBefore = auditEventRepository.count();

        LeavePreviewRequest previewReq = new LeavePreviewRequest(1L, LocalDate.of(2026, 11, 23), LocalDate.of(2026, 11, 25));
        LeavePreviewResponse preview = leaveService.preview(riya, previewReq);

        assertNotNull(preview);
        assertEquals(reqCountBefore, leaveRequestRepository.count());
        assertEquals(stepCountBefore, approvalStepRepository.count());
        assertEquals(auditCountBefore, auditEventRepository.count());
    }

    @Test
    @DisplayName("5. Apply reserves pending balance and creates initial approval step")
    void testApplyReservesPendingBalance() {
        User riya = userRepository.findByEmail("riya@co.com").orElseThrow();
        LeaveBalance balBefore = leaveBalanceRepository.findByUserIdAndLeaveTypeIdAndYear(riya.getId(), 1L, 2026).orElseThrow();
        double pendingBefore = balBefore.getPending();

        LeaveApplyRequest applyReq = new LeaveApplyRequest(1L, LocalDate.of(2026, 11, 23), LocalDate.of(2026, 11, 25), "Test Vacation");
        LeaveResponseDto response = leaveService.apply(riya, applyReq);

        assertNotNull(response.id());
        assertEquals(LeaveStatus.PENDING_MANAGER, response.status());

        LeaveBalance balAfter = leaveBalanceRepository.findByUserIdAndLeaveTypeIdAndYear(riya.getId(), 1L, 2026).orElseThrow();
        assertEquals(pendingBefore + response.days(), balAfter.getPending());
    }

    @Test
    @DisplayName("6. Insufficient balance throws 422 BusinessRuleException")
    void testInsufficientBalanceThrows422() {
        User riya = userRepository.findByEmail("riya@co.com").orElseThrow();
        // Request 15 days when available is only 12 -> throws BusinessRuleException
        LeaveApplyRequest applyReq = new LeaveApplyRequest(1L, LocalDate.of(2026, 11, 2), LocalDate.of(2026, 11, 25), "Excessive leave");
        assertThrows(BusinessRuleException.class, () -> leaveService.apply(riya, applyReq));
    }

    @Test
    @DisplayName("7. Balance deducted ONLY on HR approval (pending becomes used)")
    void testBalanceDeductedOnlyOnHrApproval() {
        User riya = userRepository.findByEmail("riya@co.com").orElseThrow();
        User manoj = userRepository.findByEmail("manager1@co.com").orElseThrow();
        User hr = userRepository.findByEmail("hr@co.com").orElseThrow();

        LeaveApplyRequest applyReq = new LeaveApplyRequest(1L, LocalDate.of(2026, 11, 23), LocalDate.of(2026, 11, 24), "Two days off");
        LeaveResponseDto submitted = leaveService.apply(riya, applyReq);

        LeaveBalance balAfterApply = leaveBalanceRepository.findByUserIdAndLeaveTypeIdAndYear(riya.getId(), 1L, 2026).orElseThrow();
        assertEquals(2.0, balAfterApply.getPending());
        assertEquals(0.0, balAfterApply.getUsed());

        // Manager approval -> status PENDING_HR, balance unchanged!
        leaveService.approveLeave(manoj, submitted.id(), new ApprovalDecisionRequest("Manager approved"));
        LeaveBalance balAfterManager = leaveBalanceRepository.findByUserIdAndLeaveTypeIdAndYear(riya.getId(), 1L, 2026).orElseThrow();
        assertEquals(2.0, balAfterManager.getPending());
        assertEquals(0.0, balAfterManager.getUsed());

        // HR approval -> status APPROVED, pending becomes used!
        leaveService.approveLeave(hr, submitted.id(), new ApprovalDecisionRequest("HR approved"));
        LeaveBalance balAfterHr = leaveBalanceRepository.findByUserIdAndLeaveTypeIdAndYear(riya.getId(), 1L, 2026).orElseThrow();
        assertEquals(0.0, balAfterHr.getPending());
        assertEquals(2.0, balAfterHr.getUsed());
    }

    @Test
    @DisplayName("8. Illegal state transition throws 409 ConflictException")
    void testIllegalTransitionThrows409() {
        User hr = userRepository.findByEmail("hr@co.com").orElseThrow();
        // Request ID 4 in seed data is already APPROVED
        assertThrows(ConflictException.class, () ->
                workflowService.transition(4L, WorkflowService.Action.APPROVE, hr, "Try to re-approve")
        );
    }

    @Test
    @DisplayName("9. Self-approve is forbidden and throws 403 AccessDeniedException")
    void testSelfApproveForbidden403() {
        User riya = userRepository.findByEmail("riya@co.com").orElseThrow();
        LeaveApplyRequest applyReq = new LeaveApplyRequest(1L, LocalDate.of(2026, 11, 23), LocalDate.of(2026, 11, 24), "Self approve attempt");
        LeaveResponseDto submitted = leaveService.apply(riya, applyReq);

        assertThrows(AccessDeniedException.class, () ->
                leaveService.approveLeave(riya, submitted.id(), new ApprovalDecisionRequest("Riya approving herself"))
        );
    }

    @Test
    @DisplayName("10. Simulate-timeout writes SYSTEM audit event and escalates to HR")
    void testSimulateTimeoutWritesSystemAudit() {
        User riya = userRepository.findByEmail("riya@co.com").orElseThrow();
        LeaveApplyRequest applyReq = new LeaveApplyRequest(1L, LocalDate.of(2026, 11, 23), LocalDate.of(2026, 11, 24), "Timeout test");
        LeaveResponseDto submitted = leaveService.apply(riya, applyReq);

        // Backdate step dueAt
        ApprovalStep step = approvalStepRepository.findByRequestIdOrderByIdAsc(submitted.id()).get(0);
        step.setDueAt(LocalDateTime.now().minusMinutes(10));
        approvalStepRepository.save(step);

        int escalatedCount = escalationService.escalateStale();
        assertTrue(escalatedCount >= 1);

        LeaveRequest req = leaveRequestRepository.findById(submitted.id()).orElseThrow();
        assertEquals(LeaveStatus.ESCALATED, req.getStatus());

        List<AuditEvent> events = auditEventRepository.findByRequestIdOrderByAtDesc(submitted.id());
        AuditEvent escalatedEvent = events.stream()
                .filter(e -> "ESCALATED".equals(e.getAction()))
                .findFirst()
                .orElseThrow();
        assertNull(escalatedEvent.getActor()); // null actor represents SYSTEM
    }

    @Test
    @DisplayName("11. Login OK with Demo@123, and fail with incorrect password")
    void testLoginOkAndFail() {
        // Success
        var auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken("riya@co.com", "Demo@123"));
        assertTrue(auth.isAuthenticated());
        org.springframework.security.core.userdetails.UserDetails userDetails = (org.springframework.security.core.userdetails.UserDetails) auth.getPrincipal();
        String token = jwtService.generateToken(userDetails);
        assertNotNull(token);
        assertTrue(jwtService.isTokenValid(token, userDetails));

        // Fail
        assertThrows(BadCredentialsException.class, () ->
                authenticationManager.authenticate(new UsernamePasswordAuthenticationToken("riya@co.com", "WrongPassword"))
        );
    }
}
