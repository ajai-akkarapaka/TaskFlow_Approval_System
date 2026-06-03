package com.taskflow.approvalsystem.repository;

import com.taskflow.approvalsystem.entity.Request;
import com.taskflow.approvalsystem.enums.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long>, org.springframework.data.jpa.repository.JpaSpecificationExecutor<Request> {
    List<Request> findByRequesterIdAndStatus(Long requesterId, RequestStatus status);
}
