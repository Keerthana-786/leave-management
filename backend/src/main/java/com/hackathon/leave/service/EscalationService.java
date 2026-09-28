package com.hackathon.leave.service;

import com.hackathon.leave.model.ApprovalDecision;
import com.hackathon.leave.model.ApprovalStep;
import com.hackathon.leave.model.LeaveRequest;
import com.hackathon.leave.model.LeaveStatus;
import com.hackathon.leave.repository.ApprovalStepRepository;
import com.hackathon.leave.repository.LeaveRequestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EscalationService {

    private static final Logger log = LoggerFactory.getLogger(EscalationService.class);

    private final LeaveRequestRepository leaveRequestRepository;
    private final ApprovalStepRepository approvalStepRepository;
    private final WorkflowService workflowService;

    public EscalationService(
            LeaveRequestRepository leaveRequestRepository,
            ApprovalStepRepository approvalStepRepository,
            WorkflowService workflowService
    ) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.approvalStepRepository = approvalStepRepository;
        this.workflowService = workflowService;
    }

    @Scheduled(fixedDelayString = "${leave.escalation-check-seconds:30}000")
    @Transactional
    public int escalateStale() {
        List<LeaveRequest> pendingRequests = leaveRequestRepository.findAll().stream()
                .filter(r -> r.getStatus() == LeaveStatus.PENDING_MANAGER)
                .toList();

        int count = 0;
        LocalDateTime now = LocalDateTime.now();

        for (LeaveRequest request : pendingRequests) {
            List<ApprovalStep> steps = approvalStepRepository.findByRequestIdOrderByIdAsc(request.getId());
            for (ApprovalStep step : steps) {
                if (step.getDecision() == ApprovalDecision.PENDING && step.getDueAt() != null && step.getDueAt().isBefore(now)) {
                    log.info("Escalating request #{} due to SLA expiration (dueAt={})", request.getId(), step.getDueAt());
                    workflowService.transition(
                            request.getId(),
                            WorkflowService.Action.ESCALATE,
                            null, // actor null = SYSTEM
                            "Auto-escalated to HR due to manager SLA timeout"
                    );
                    count++;
                    break;
                }
            }
        }

        return count;
    }
}
