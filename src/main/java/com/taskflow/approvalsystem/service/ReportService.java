package com.taskflow.approvalsystem.service;

import com.taskflow.approvalsystem.dto.ReportSummary;
import com.taskflow.approvalsystem.entity.Request;
import com.taskflow.approvalsystem.enums.RequestStatus;
import com.taskflow.approvalsystem.enums.RequestType;
import com.taskflow.approvalsystem.repository.RequestRepository;
import com.taskflow.approvalsystem.repository.ApprovalStepRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.EnumMap;
import java.util.List;

@Service
public class ReportService {

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private ApprovalStepRepository approvalStepRepository;

    //Build a simple report summary. If start or end is null, they are treated as unbounded.

    public ReportSummary getSummary(LocalDate startDate, LocalDate endDate) {
        List<Request> all = requestRepository.findAll();

        ReportSummary summary = new ReportSummary();

        long total = 0;
        EnumMap<RequestStatus, Long> statusCounts = new EnumMap<>(RequestStatus.class);
        EnumMap<RequestType, Long> typeCounts = new EnumMap<>(RequestType.class);

        // For approval time metrics
        double totalHoursToFirstApproval = 0.0;
        int countFirstApproval = 0;
        double totalHoursToFinalApproval = 0.0;
        int countFinalApproval = 0;
        double totalHoursPerStep = 0.0;
        int countStepIntervals = 0;

        // initialize maps so JSON always contains keys
        for (RequestStatus s : RequestStatus.values()) statusCounts.put(s, 0L);
        for (RequestType t : RequestType.values()) typeCounts.put(t, 0L);

        for (Request r : all) {
            LocalDate created = r.getCreatedAt() != null ? r.getCreatedAt().toLocalDate() : null;
            boolean inWindow = true;
            if (startDate != null && (created == null || created.isBefore(startDate))) inWindow = false;
            if (endDate != null && (created == null || created.isAfter(endDate))) inWindow = false;
            if (!inWindow) continue;

            total++;
            statusCounts.put(r.getStatus(), statusCounts.getOrDefault(r.getStatus(), 0L) + 1);
            typeCounts.put(r.getType(), typeCounts.getOrDefault(r.getType(), 0L) + 1);

            // Approval time calculations using approval steps
            if (r.getCreatedAt() != null) {
                List<com.taskflow.approvalsystem.entity.ApprovalStep> steps = approvalStepRepository.findByRequestIdOrderByStepOrder(r.getId());
                // consider only steps that have approvedAt
                java.time.LocalDateTime createdAt = r.getCreatedAt();
                java.time.LocalDateTime firstApprovedAt = null;
                java.time.LocalDateTime lastApprovedAt = null;
                java.time.LocalDateTime prevTimestamp = createdAt;
                for (com.taskflow.approvalsystem.entity.ApprovalStep s : steps) {
                    if (s.getApprovedAt() != null) {
                        if (firstApprovedAt == null) firstApprovedAt = s.getApprovedAt();
                        lastApprovedAt = s.getApprovedAt();
                        // interval for this step = s.approvedAt - prevTimestamp
                        if (prevTimestamp != null) {
                            java.time.Duration d = java.time.Duration.between(prevTimestamp, s.getApprovedAt());
                            totalHoursPerStep += d.toMinutes() / 60.0;
                            countStepIntervals++;
                        }
                        prevTimestamp = s.getApprovedAt();
                    }
                }
                if (firstApprovedAt != null) {
                    java.time.Duration d = java.time.Duration.between(createdAt, firstApprovedAt);
                    totalHoursToFirstApproval += d.toMinutes() / 60.0;
                    countFirstApproval++;
                }
                if (lastApprovedAt != null) {
                    java.time.Duration d = java.time.Duration.between(createdAt, lastApprovedAt);
                    totalHoursToFinalApproval += d.toMinutes() / 60.0;
                    countFinalApproval++;
                }
            }
        }

        summary.setTotalRequests(total);
        summary.setByStatus(statusCounts);
        summary.setByType(typeCounts);
        // compute averages, protect divide by zero
        summary.setAvgTimeToFirstApprovalHours(countFirstApproval > 0 ? totalHoursToFirstApproval / countFirstApproval : 0.0);
        summary.setAvgTimeToFinalApprovalHours(countFinalApproval > 0 ? totalHoursToFinalApproval / countFinalApproval : 0.0);
        summary.setAvgTimePerStepHours(countStepIntervals > 0 ? totalHoursPerStep / countStepIntervals : 0.0);
        return summary;
    }
}


