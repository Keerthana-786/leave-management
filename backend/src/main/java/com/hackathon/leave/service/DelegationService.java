package com.hackathon.leave.service;

import com.hackathon.leave.dto.DelegationCreateRequest;
import com.hackathon.leave.dto.DelegationDto;
import com.hackathon.leave.exception.BusinessRuleException;
import com.hackathon.leave.exception.ResourceNotFoundException;
import com.hackathon.leave.model.Delegation;
import com.hackathon.leave.model.Role;
import com.hackathon.leave.model.User;
import com.hackathon.leave.repository.DelegationRepository;
import com.hackathon.leave.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class DelegationService {

    private final DelegationRepository delegationRepository;
    private final UserRepository userRepository;

    public DelegationService(DelegationRepository delegationRepository, UserRepository userRepository) {
        this.delegationRepository = delegationRepository;
        this.userRepository = userRepository;
    }

    public record ResolvedAssignee(User effectiveAssignee, User delegatedFrom) {}

    public ResolvedAssignee resolveAssignee(User intendedAssignee, LocalDate date) {
        if (intendedAssignee == null) {
            return new ResolvedAssignee(null, null);
        }
        LocalDate checkDate = date != null ? date : LocalDate.now();
        Optional<Delegation> delegationOpt = delegationRepository
                .findByDelegatorIdAndActiveTrueAndFromDateLessThanEqualAndToDateGreaterThanEqual(
                        intendedAssignee.getId(), checkDate, checkDate
                );

        if (delegationOpt.isPresent()) {
            Delegation delegation = delegationOpt.get();
            return new ResolvedAssignee(delegation.getDelegate(), intendedAssignee);
        }
        return new ResolvedAssignee(intendedAssignee, null);
    }

    @Transactional
    public DelegationDto createDelegation(User delegator, DelegationCreateRequest request) {
        if (request.fromDate().isAfter(request.toDate())) {
            throw new BusinessRuleException("From date must be on or before to date");
        }
        if (delegator.getId().equals(request.delegateId())) {
            throw new BusinessRuleException("Cannot delegate authority to yourself");
        }

        User delegate = userRepository.findById(request.delegateId())
                .orElseThrow(() -> new ResourceNotFoundException("Delegate user not found"));

        Delegation delegation = new Delegation();
        delegation.setDelegator(delegator);
        delegation.setDelegate(delegate);
        delegation.setFromDate(request.fromDate());
        delegation.setToDate(request.toDate());
        delegation.setActive(true);

        Delegation saved = delegationRepository.save(delegation);
        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public List<DelegationDto> getDelegations(User user) {
        List<Delegation> list;
        if (user.getRole() == Role.HR) {
            list = delegationRepository.findAll();
        } else {
            list = new ArrayList<>(delegationRepository.findByDelegatorIdAndActiveTrue(user.getId()));
            for (Delegation d : delegationRepository.findByDelegateIdAndActiveTrue(user.getId())) {
                if (!list.contains(d)) {
                    list.add(d);
                }
            }
        }
        return list.stream().map(this::toDto).toList();
    }

    @Transactional
    public void revokeDelegation(User user, Long id) {
        Delegation delegation = delegationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Delegation not found"));

        if (!delegation.getDelegator().getId().equals(user.getId()) && user.getRole() != Role.HR) {
            throw new AccessDeniedException("You can only revoke your own delegations");
        }

        delegation.setActive(false);
        delegationRepository.save(delegation);
    }

    public DelegationDto toDto(Delegation d) {
        return new DelegationDto(
                d.getId(),
                d.getDelegator().getId(),
                d.getDelegator().getName(),
                d.getDelegate().getId(),
                d.getDelegate().getName(),
                d.getFromDate(),
                d.getToDate(),
                d.isActive()
        );
    }
}
