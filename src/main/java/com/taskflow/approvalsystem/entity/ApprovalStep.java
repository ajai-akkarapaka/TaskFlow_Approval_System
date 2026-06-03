package com.taskflow.approvalsystem.entity;

import com.taskflow.approvalsystem.enums.ApprovalStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "approval_steps")
public class ApprovalStep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "request_id")
    private Request request;

    @NotNull
    private Integer stepOrder;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "approver_id")
    private User approver;

    @NotNull
    @Enumerated(EnumType.STRING)
    private ApprovalStatus status = ApprovalStatus.PENDING;

    private String comments;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    // Constructors, getters, setters

    public ApprovalStep() {}

    public ApprovalStep(Request request, Integer stepOrder, User approver) {
        this.request = request;
        this.stepOrder = stepOrder;
        this.approver = approver;
    }
    public boolean isAssignedTo(Long userId) {
        return this.approver != null && this.approver.getId().equals(userId);
    }

    public void approve(String comments) {
        this.status = ApprovalStatus.APPROVED;
        this.comments = comments;
        this.approvedAt = java.time.LocalDateTime.now();
    }

    public void reject(String comments) {
        this.status = ApprovalStatus.REJECTED;
        this.comments = comments;
        this.approvedAt = java.time.LocalDateTime.now();
    }

    public void cancel(String comments) {
        this.status = ApprovalStatus.CANCELLED;
        this.comments = comments;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Request getRequest() { return request; }
    public void setRequest(Request request) { this.request = request; }

    public Integer getStepOrder() { return stepOrder; }
    public void setStepOrder(Integer stepOrder) { this.stepOrder = stepOrder; }

    public User getApprover() { return approver; }
    public void setApprover(User approver) { this.approver = approver; }

    public ApprovalStatus getStatus() { return status; }
    public void setStatus(ApprovalStatus status) { this.status = status; }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }

    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }
}
