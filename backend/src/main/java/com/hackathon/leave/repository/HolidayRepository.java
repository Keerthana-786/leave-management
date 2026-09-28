package com.hackathon.leave.repository;

import com.hackathon.leave.model.Holiday;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface HolidayRepository extends JpaRepository<Holiday, Long> {
    Optional<Holiday> findByDate(LocalDate date);
    List<Holiday> findByDateBetweenOrderByDateAsc(LocalDate fromDate, LocalDate toDate);
}
