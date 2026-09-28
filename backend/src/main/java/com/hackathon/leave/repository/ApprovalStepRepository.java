package com.hackathon.leave.repository;

import com.hackathon.leave.model.ApprovalDecision;
import com.hackathon.leave.model.ApprovalStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApprovalStepRepository extends JpaRepository<ApprovalStep, Long> {
    List<ApprovalStep> findByRequestIdOrderByIdAsc(Long requestId);
    List<ApprovalStep> findByAssigneeIdAndDecision(Long assigneeId, ApprovalDecision decision);
}
