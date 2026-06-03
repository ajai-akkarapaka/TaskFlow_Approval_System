package com.taskflow.approvalsystem.controller;

import com.taskflow.approvalsystem.entity.ApprovalStep;
import com.taskflow.approvalsystem.entity.Request;
import com.taskflow.approvalsystem.enums.RequestType;
import com.taskflow.approvalsystem.service.RequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import com.taskflow.approvalsystem.util.DateUtils;
import java.util.List;

@RestController
@RequestMapping("/api/requests")
@Tag(name = "Request Management", description = "APIs for managing approval requests")
public class RequestController {

    @Autowired
    private RequestService requestService;

    @PostMapping
    @Operation(summary = "Create a new request", description = "Creates a new draft request")
    public ResponseEntity<Request> createRequest(@RequestParam String title, @RequestParam(required = false) String description, @RequestParam RequestType type, @RequestParam Long requesterId, @RequestParam(required = false) BigDecimal amount, @Parameter(description = "Start date in DDMMYYYY format (e.g., 21052026)", example = "21052026") @RequestParam(required = false) String startDateStr, @Parameter(description = "End date in DDMMYYYY format (e.g., 25052026)", example = "25052026") @RequestParam(required = false) String endDateStr) {
        try {
            LocalDate startDate = DateUtils.parseOptionalDate(startDateStr);
            LocalDate endDate = DateUtils.parseOptionalDate(endDateStr);
            Request request = requestService.createRequest(title, description, type, requesterId, amount, startDate, endDate);
            return ResponseEntity.ok(request);
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest().body(null); // Could return error message
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a draft request", description = "Updates an existing draft request")
    public ResponseEntity<Request> updateDraft(@PathVariable Long id, @RequestParam String title, @RequestParam(required = false) String description, @RequestParam(required = false) BigDecimal amount, @Parameter(description = "Start date in DDMMYYYY format (e.g., 21052026)") @RequestParam(required = false) String startDateStr, @Parameter(description = "End date in DDMMYYYY format (e.g., 25052026)") @RequestParam(required = false) String endDateStr, @RequestParam Long userId) {
        try {
            LocalDate startDate = DateUtils.parseOptionalDate(startDateStr);
            LocalDate endDate = DateUtils.parseOptionalDate(endDateStr);
            Request request = requestService.updateDraft(id, title, description, amount, startDate, endDate, userId);
            return ResponseEntity.ok(request);
        } catch (IllegalArgumentException | DateTimeParseException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{id}/submit")
    @Operation(summary = "Submit a request", description = "Submits a draft request for approval")
    public ResponseEntity<Request> submitRequest(@PathVariable Long id, @RequestParam Long userId) {
        try {
            Request request = requestService.submitRequest(id, userId);
            return ResponseEntity.ok(request);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{id}/approve")
    @Operation(summary = "Approve the request", description = "Approves the current approval request")
    public ResponseEntity<Void> approveStep(@PathVariable Long id, @RequestParam Long userId, @RequestParam(required = false) String comments) {
        try {
            requestService.approveStep(id, userId, comments);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{id}/reject")
    @Operation(summary = "Reject a request", description = "Rejects the current approval request")
    public ResponseEntity<Void> rejectStep(@PathVariable Long id, @RequestParam Long userId, @RequestParam String comments) {
        try {
            requestService.rejectStep(id, userId, comments);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel a request", description = "Cancels a submitted request")
    public ResponseEntity<Void> cancelRequest(@PathVariable Long id, @RequestParam Long userId) {
        try {
            requestService.cancelRequest(id, userId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/pending")
    @Operation(summary = "Get pending approvals", description = "Retrieves pending approval steps for a user")
    public List<ApprovalStep> getPendingApprovals(@RequestParam Long userId) {
        return requestService.getPendingApprovals(userId);
    }

    @GetMapping
    @Operation(summary = "Get user requests", description = "Retrieves requests for a specific user")
    public List<Request> getRequests(@RequestParam Long userId) {
        return requestService.getRequestsByUser(userId);
    }

    @GetMapping("/search")
    @Operation(summary = "Search requests", description = "Search requests with filters: type, status, department, dates (ddMMyyyy), amount range. pagination via page and size.")
    public org.springframework.http.ResponseEntity<org.springframework.data.domain.Page<Request>> searchRequests(
            @RequestParam(required = false) RequestType type,
            @RequestParam(required = false) com.taskflow.approvalsystem.enums.RequestStatus status,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String startDateStr,
            @RequestParam(required = false) String endDateStr,
            @RequestParam(required = false) java.math.BigDecimal minAmount,
            @RequestParam(required = false) java.math.BigDecimal maxAmount,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        try {
            java.time.LocalDate start = DateUtils.parseOptionalDate(startDateStr);
            java.time.LocalDate end = DateUtils.parseOptionalDate(endDateStr);
            org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(Math.max(0, page), Math.max(1, size));
            org.springframework.data.domain.Page<Request> result = requestService.searchRequests(type, status, department, start, end, minAmount, maxAmount, pageable);
            return org.springframework.http.ResponseEntity.ok(result);
        } catch (java.time.format.DateTimeParseException e) {
            return org.springframework.http.ResponseEntity.badRequest().build();
        }
    }
}