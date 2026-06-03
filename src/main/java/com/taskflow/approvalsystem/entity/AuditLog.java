package com.taskflow.approvalsystem.entity;

import com.taskflow.approvalsystem.enums.AuditAction;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "request_id")
    private Request request;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @NotNull
    @Enumerated(EnumType.STRING)
    private AuditAction action;

    @NotNull
    private LocalDateTime timestamp;

    private String details;

    // Constructors, getters, setters

    public AuditLog() {}

    public AuditLog(Request request, User user, AuditAction action, String details) {
        this.request = request;
        this.user = user;
        this.action = action;
        this.timestamp = LocalDateTime.now();
        this.details = details;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Request getRequest() { return request; }
    public void setRequest(Request request) { this.request = request; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public AuditAction getAction() { return action; }
    public void setAction(AuditAction action) { this.action = action; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
}
