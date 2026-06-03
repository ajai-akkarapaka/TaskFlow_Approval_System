package com.taskflow.approvalsystem.dto;

import com.taskflow.approvalsystem.enums.RequestStatus;
import com.taskflow.approvalsystem.enums.RequestType;

import java.util.HashMap;
import java.util.Map;

public class ReportSummary {

    private long totalRequests;
    private Map<RequestStatus, Long> byStatus = new HashMap<>();
    private Map<RequestType, Long> byType = new HashMap<>();
    // Approval time metrics (hours)
    private double avgTimeToFirstApprovalHours;
    private double avgTimeToFinalApprovalHours;
    private double avgTimePerStepHours;

    public ReportSummary() {}

    public long getTotalRequests() { return totalRequests; }
    public void setTotalRequests(long totalRequests) { this.totalRequests = totalRequests; }

    public Map<RequestStatus, Long> getByStatus() { return byStatus; }
    public void setByStatus(Map<RequestStatus, Long> byStatus) { this.byStatus = byStatus; }

    public Map<RequestType, Long> getByType() { return byType; }
    public void setByType(Map<RequestType, Long> byType) { this.byType = byType; }

    public double getAvgTimeToFirstApprovalHours() { return avgTimeToFirstApprovalHours; }
    public void setAvgTimeToFirstApprovalHours(double avgTimeToFirstApprovalHours) { this.avgTimeToFirstApprovalHours = avgTimeToFirstApprovalHours; }

    public double getAvgTimeToFinalApprovalHours() { return avgTimeToFinalApprovalHours; }
    public void setAvgTimeToFinalApprovalHours(double avgTimeToFinalApprovalHours) { this.avgTimeToFinalApprovalHours = avgTimeToFinalApprovalHours; }

    public double getAvgTimePerStepHours() { return avgTimePerStepHours; }
    public void setAvgTimePerStepHours(double avgTimePerStepHours) { this.avgTimePerStepHours = avgTimePerStepHours; }
}


