package com.taskflow.approvalsystem.controller;

import com.taskflow.approvalsystem.dto.ReportSummary;
import com.taskflow.approvalsystem.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import com.taskflow.approvalsystem.util.DateUtils;

@RestController
@RequestMapping("/api/reports")
@Tag(name = "Reports", description = "APIs for retrieving reports and summaries")
public class ReportsController {

    @Autowired
    private ReportService reportService;

    @GetMapping("/summary")
    @Operation(summary = "Get report summary", description = "Returns summary counts for requests in an optional date range (ddMMyyyy)")
    public org.springframework.http.ResponseEntity<ReportSummary> getSummary(@RequestParam(required = false) String startDateStr, @RequestParam(required = false) String endDateStr) {
        try {
            LocalDate start = DateUtils.parseOptionalDate(startDateStr);
            LocalDate end = DateUtils.parseOptionalDate(endDateStr);
            ReportSummary summary = reportService.getSummary(start, end);
            return org.springframework.http.ResponseEntity.ok(summary);
        } catch (DateTimeParseException e) {
            return org.springframework.http.ResponseEntity.badRequest().build();
        }
    }
}


