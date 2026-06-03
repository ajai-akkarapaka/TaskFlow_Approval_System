package com.taskflow.approvalsystem.controller;

import com.taskflow.approvalsystem.entity.AuditLog;
import com.taskflow.approvalsystem.repository.AuditLogRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/audit")
@Tag(name = "Audit Logs", description = "APIs for retrieving audit logs")
public class AuditController {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @GetMapping("/request/{requestId}")
    @Operation(summary = "Get audit logs for a request", description = "Retrieves audit logs for a specific request")
    public List<AuditLog> getAuditLogs(@PathVariable Long requestId) {
        return auditLogRepository.findByRequestIdOrderByTimestamp(requestId);
    }
}
