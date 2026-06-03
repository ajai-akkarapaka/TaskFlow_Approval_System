package com.taskflow.approvalsystem.repository;

import com.taskflow.approvalsystem.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByRequestIdOrderByTimestamp(Long requestId);
}
