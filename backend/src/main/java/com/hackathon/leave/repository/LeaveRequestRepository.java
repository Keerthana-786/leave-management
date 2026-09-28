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

    @org.springframework.data.jpa.repository.Query("SELECT lr FROM LeaveRequest lr WHERE lr.employee.id IN :employeeIds AND lr.status IN :statuses AND lr.fromDate <= :toDate AND lr.toDate >= :fromDate")
    List<LeaveRequest> findOverlappingRequests(
            @org.springframework.data.repository.query.Param("employeeIds") List<Long> employeeIds,
            @org.springframework.data.repository.query.Param("statuses") List<LeaveStatus> statuses,
            @org.springframework.data.repository.query.Param("fromDate") LocalDate fromDate,
            @org.springframework.data.repository.query.Param("toDate") LocalDate toDate
    );

    @org.springframework.data.jpa.repository.Query("SELECT lr FROM LeaveRequest lr WHERE lr.employee.id = :employeeId AND lr.status IN :statuses AND lr.fromDate <= :toDate AND lr.toDate >= :fromDate")
    List<LeaveRequest> findOwnOverlappingRequests(
            @org.springframework.data.repository.query.Param("employeeId") Long employeeId,
            @org.springframework.data.repository.query.Param("statuses") List<LeaveStatus> statuses,
            @org.springframework.data.repository.query.Param("fromDate") LocalDate fromDate,
            @org.springframework.data.repository.query.Param("toDate") LocalDate toDate
    );
}
