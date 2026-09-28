package com.hackathon.leave.repository;

import com.hackathon.leave.model.EmailOutbox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmailOutboxRepository extends JpaRepository<EmailOutbox, Long> {
    List<EmailOutbox> findBySentFalseOrderByCreatedAtAsc();
}
