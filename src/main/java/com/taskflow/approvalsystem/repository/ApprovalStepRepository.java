package com.taskflow.approvalsystem.repository;

import com.taskflow.approvalsystem.entity.ApprovalStep;
import com.taskflow.approvalsystem.enums.ApprovalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApprovalStepRepository extends JpaRepository<ApprovalStep, Long> {
    List<ApprovalStep> findByRequestIdOrderByStepOrder(Long requestId);
    Optional<ApprovalStep> findByRequestIdAndStepOrder(Long requestId, Integer stepOrder);
    List<ApprovalStep> findByApproverIdAndStatus(Long approverId, ApprovalStatus status);
}
