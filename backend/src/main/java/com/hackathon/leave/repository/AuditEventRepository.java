package com.hackathon.leave.repository;

import com.hackathon.leave.model.AuditEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditEventRepository extends JpaRepository<AuditEvent, Long> {
    List<AuditEvent> findByRequestIdOrderByAtDesc(Long requestId);
}
