package com.taskflow.approvalsystem.workflow;

import com.taskflow.approvalsystem.entity.*;
import com.taskflow.approvalsystem.enums.*;
import java.math.BigDecimal;
import java.util.*;

public class ApprovalWorkflowGenerator {

    private static final BigDecimal EXPENSE_THRESHOLD = BigDecimal.valueOf(50000);
    private static final BigDecimal ASSET_THRESHOLD = BigDecimal.valueOf(100000);

    public static List<User> determineApprovers(Request request, User admin, User finance) {
        List<User> approvers = new ArrayList<>();
        User requester = request.getRequester();
        // manager approval
        if (requester.getManager() != null) {
            approvers.add(requester.getManager());
        } else if (requester.getRole() == Role.MANAGER) {
            approvers.add(admin);
        } else {
            throw new IllegalArgumentException("Requester has no manager hierarchy assigned.");
        }
        // conditional routing based on business logic
        switch (request.getType()) {
            case EXPENSE -> {
                if (request.getAmount().compareTo(EXPENSE_THRESHOLD) > 0) {
                    approvers.add(finance);
                }
            }
            case ACCESS -> approvers.add(admin);
            case ASSET -> {
                if (request.getAmount().compareTo(ASSET_THRESHOLD) > 0) {
                    approvers.add(finance);
                    approvers.add(admin);
                }
            }
        }
        // De-duplicate while preserving insertion order (example: if manager is also finance)
        return approvers.stream().distinct().toList();
    }
}