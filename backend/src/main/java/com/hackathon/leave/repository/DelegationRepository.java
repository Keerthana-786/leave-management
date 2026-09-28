package com.hackathon.leave.repository;

import com.hackathon.leave.model.Delegation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DelegationRepository extends JpaRepository<Delegation, Long> {
    List<Delegation> findByDelegatorIdAndActiveTrue(Long delegatorId);
    List<Delegation> findByDelegateIdAndActiveTrue(Long delegateId);
    Optional<Delegation> findByDelegatorIdAndActiveTrueAndFromDateLessThanEqualAndToDateGreaterThanEqual(
            Long delegatorId, LocalDate now1, LocalDate now2
    );
}
