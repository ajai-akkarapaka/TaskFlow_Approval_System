package com.taskflow.approvalsystem.service;

import com.taskflow.approvalsystem.entity.*;
import com.taskflow.approvalsystem.enums.*;
import com.taskflow.approvalsystem.repository.*;
import com.taskflow.approvalsystem.workflow.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;
import java.time.LocalDateTime;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;

@Service
public class RequestService {

    @Autowired private RequestRepository requestRepository;
    @Autowired private ApprovalStepRepository approvalStepRepository;
    @Autowired private AuditLogRepository auditLogRepository;
    @Autowired private UserRepository userRepository;

    @Transactional
    public Request createRequest(String title, String description, RequestType type, @NotNull Long requesterId, BigDecimal amount, LocalDate startDate, LocalDate endDate) {
        User requester = userRepository.findById(Objects.requireNonNull(requesterId))
                .orElseThrow(() -> new IllegalArgumentException("Requester not found"));

        Request request = new Request(title, description, type, requester, amount, startDate, endDate);
        requestRepository.save(request);

        logAudit(request, requester, AuditAction.CREATE, "Request created in draft status");
        return request;
    }

    @Transactional
    public Request updateDraft(Long requestId, String title, String description, BigDecimal amount, LocalDate startDate, LocalDate endDate, Long userId) {
        Request request = findRequest(requestId);

        if (!request.isOwnedBy(userId)) throw new IllegalArgumentException("Only the requester can modify this draft");
        if (!request.isDraft()) throw new IllegalArgumentException("Modifications are only allowed on draft requests");

        request.updateDetails(title, description, amount, startDate, endDate);
        requestRepository.save(request);

        logAudit(request, request.getRequester(), AuditAction.UPDATE, "Draft details updated");
        return request;
    }

    @Transactional
    public Request submitRequest(Long requestId, Long userId) {
        Request request = findRequest(requestId);

        if (!request.isOwnedBy(userId)) throw new IllegalArgumentException("Only the requester can submit this request");
        if (!request.isDraft()) throw new IllegalArgumentException("This request has already been submitted");

        validateRequiredFields(request);
        buildApprovalWorkflow(request);

        request.setStatus(RequestStatus.SUBMITTED);
        requestRepository.save(request);

        logAudit(request, request.getRequester(), AuditAction.SUBMIT, "Request submitted for approval");
        return request;
    }

    @Transactional
    public void approveStep(Long requestId, Long userId, String comments) {
        ApprovalStep currentStep = findCurrentPendingStep(requestId);
        if (!currentStep.isAssignedTo(userId)) throw new IllegalArgumentException("You are not the designated approver for this step");

        currentStep.approve(comments);
        approvalStepRepository.save(currentStep);
        logAudit(currentStep.getRequest(), currentStep.getApprover(), AuditAction.APPROVE, "Step approved: " + comments);

        // Finalize request status if all steps completed
        List<ApprovalStep> workflow = approvalStepRepository.findByRequestIdOrderByStepOrder(requestId);
        boolean isWorkflowComplete = workflow.stream().allMatch(step -> step.getStatus() == ApprovalStatus.APPROVED);

        if (isWorkflowComplete) {
            Request request = currentStep.getRequest();
            request.setStatus(RequestStatus.APPROVED);
            requestRepository.save(request);
            logAudit(request, currentStep.getApprover(), AuditAction.APPROVE, "Request fully approved and closed");
        }
    }

    @Transactional
    public void rejectStep(Long requestId, Long userId, String comments) {
        ApprovalStep currentStep = findCurrentPendingStep(requestId);
        if (!currentStep.isAssignedTo(userId)) throw new IllegalArgumentException("You are not the designated approver for this step");

        currentStep.reject(comments);
        approvalStepRepository.save(currentStep);

        Request request = currentStep.getRequest();
        request.setStatus(RequestStatus.REJECTED);
        requestRepository.save(request);

        logAudit(request, currentStep.getApprover(), AuditAction.REJECT, "Request rejected at step: " + comments);
    }

    @Transactional
    public void cancelRequest(Long requestId, Long userId) {
        Request request = findRequest(requestId);

        if (!request.isOwnedBy(userId)) throw new IllegalArgumentException("Only the requester can cancel this request");
        if (request.isFinalized()) throw new IllegalArgumentException("Cannot cancel a completed or rejected request");

        request.setStatus(RequestStatus.CANCELLED);
        requestRepository.save(request);

        // Invalidate active steps
        List<ApprovalStep> steps = approvalStepRepository.findByRequestIdOrderByStepOrder(requestId);
        steps.stream()
                .filter(step -> step.getStatus() == ApprovalStatus.PENDING)
                .forEach(step -> step.cancel("Cancelled by requester"));

        approvalStepRepository.saveAll(Objects.requireNonNull(steps));
        logAudit(request, request.getRequester(), AuditAction.CANCEL, "Request cancelled by author");
    }

    public List<ApprovalStep> getPendingApprovals(Long userId) {
        return approvalStepRepository.findByApproverIdAndStatus(userId, ApprovalStatus.PENDING);
    }

    public List<Request> getRequestsByUser(Long userId) {
        return requestRepository.findByRequesterIdAndStatus(Objects.requireNonNull(userId), RequestStatus.DRAFT);
    }

    //Search requests with optional filters and pagination.
    public org.springframework.data.domain.Page<Request> searchRequests(
            RequestType type,
            RequestStatus status,
            String department,
            LocalDate startDate,
            LocalDate endDate,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            @NotNull Pageable pageable) {

            Specification<Request> spec = (root, query, cb) -> {
            Predicate p = cb.conjunction();

            if (type != null) {
                p = cb.and(p, cb.equal(root.get("type"), type));
            }
            if (status != null) {
                p = cb.and(p, cb.equal(root.get("status"), status));
            }
            if (department != null && !department.isBlank()) {
                p = cb.and(p, cb.equal(root.get("requester").get("department"), department));
            }
            if (startDate != null) {
                LocalDateTime start = startDate.atStartOfDay();
                p = cb.and(p, cb.greaterThanOrEqualTo(root.get("createdAt"), start));
            }
            if (endDate != null) {
                LocalDateTime end = endDate.atTime(23, 59, 59);
                p = cb.and(p, cb.lessThanOrEqualTo(root.get("createdAt"), end));
            }
            if (minAmount != null) {
                p = cb.and(p, cb.greaterThanOrEqualTo(root.get("amount"), minAmount));
            }
            if (maxAmount != null) {
                p = cb.and(p, cb.lessThanOrEqualTo(root.get("amount"), maxAmount));
            }

            return p;
        };

        return requestRepository.findAll(spec, Objects.requireNonNull(pageable));
    }

    private Request findRequest(Long id) {
        return requestRepository.findById(Objects.requireNonNull(id)).orElseThrow(() -> new IllegalArgumentException("Request not found"));
    }

    private ApprovalStep findCurrentPendingStep(Long requestId) {
        return approvalStepRepository.findByRequestIdOrderByStepOrder(Objects.requireNonNull(requestId)).stream()
                .filter(step -> step.getStatus() == ApprovalStatus.PENDING)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No pending approval steps found for this request"));
    }

    private void validateRequiredFields(Request request) {
        switch (request.getType()) {
            case EXPENSE -> { if (request.getAmount() == null) throw new IllegalArgumentException("Amount is required for expense requests"); }
            case LEAVE -> { if (request.getStartDate() == null || request.getEndDate() == null) throw new IllegalArgumentException("Start and End dates are required for leaves"); }
            case ASSET -> { if (request.getAmount() == null) throw new IllegalArgumentException("Amount is required for asset requests"); }
            case ACCESS -> { /* No additional fields required */ }
        }
    }

    private void buildApprovalWorkflow(Request request) {
        User admin = findUserByRole(Role.ADMIN);
        User finance = findUserByRole(Role.FINANCE);

        List<User> sequenceOfApprovers = ApprovalWorkflowGenerator.determineApprovers(request, admin, finance);

        // Convert sequential user list into Step entities with indexed orders
        List<ApprovalStep> steps = IntStream.range(0, sequenceOfApprovers.size())
                .mapToObj(index -> new ApprovalStep(request, index + 1, sequenceOfApprovers.get(index)))
                .toList();

        approvalStepRepository.saveAll(Objects.requireNonNull(steps));
    }

    private User findUserByRole(Role role) {
        return userRepository.findAll().stream()
                .filter(u -> u.getRole() == role)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No user found with system role: " + role));
    }

    private void logAudit(Request request, User actor, AuditAction action, String notes) {
        auditLogRepository.save(new AuditLog(request, actor, action, notes));
    }
}