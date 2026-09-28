package com.hackathon.leave.repository;

import com.hackathon.leave.model.LeaveRequest;
import com.hackathon.leave.model.LeaveStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
    List<LeaveRequest> findByEmployeeId(Long employeeId);
    Page<LeaveRequest> findByStatus(LeaveStatus status, Pageable pageable);
    List<LeaveRequest> findByStatusAndFromDateLessThanEqualAndToDateGreaterThanEqual(
            LeaveStatus status, LocalDate toDate, LocalDate fromDate
    );
}
