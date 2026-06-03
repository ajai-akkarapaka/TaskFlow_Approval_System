package com.taskflow.approvalsystem.entity;

import com.taskflow.approvalsystem.enums.RequestStatus;
import com.taskflow.approvalsystem.enums.RequestType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "requests")
public class Request {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String title;

    private String description;

    @NotNull
    @Enumerated(EnumType.STRING)
    private RequestType type;

    @NotNull
    @Enumerated(EnumType.STRING)
    private RequestStatus status = RequestStatus.DRAFT;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "requester_id")
    private User requester;

    private BigDecimal amount;

    private LocalDate startDate; // for leaves

    private LocalDate endDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors, getters, setters

    public Request() {}

    public Request(String title, String description, RequestType type, User requester, BigDecimal amount, LocalDate startDate, LocalDate endDate) {
        this.title = title;
        this.description = description;
        this.type = type;
        this.requester = requester;
        this.amount = amount;
        this.startDate = startDate;
        this.endDate = endDate;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    public boolean isOwnedBy(Long userId) {
        return this.requester != null && this.requester.getId().equals(userId);
    }

    public boolean isDraft() {
        return this.status == RequestStatus.DRAFT;
    }

    public boolean isFinalized() {
        return this.status == RequestStatus.APPROVED || this.status == RequestStatus.REJECTED;
    }
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public RequestType getType() { return type; }
    public void setType(RequestType type) { this.type = type; }

    public RequestStatus getStatus() { return status; }
    public void setStatus(RequestStatus status) { this.status = status; }

    public User getRequester() { return requester; }
    public void setRequester(User requester) { this.requester = requester; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void updateDetails(String title, String description, BigDecimal amount, LocalDate startDate, LocalDate endDate) {
        this.title = title;
        this.description = description;
        this.amount = amount;
        this.startDate = startDate;
        this.endDate = endDate;
    }
}