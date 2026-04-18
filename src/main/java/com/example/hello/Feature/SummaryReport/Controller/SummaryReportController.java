package com.example.hello.Feature.SummaryReport.Controller;

import com.example.hello.Feature.SummaryReport.Service.SummaryReportService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("summary-report")
public class SummaryReportController {
    SummaryReportService summaryReportService;

    @PreAuthorize("hasAnyAuthority('GET_SUMMARY_REPORT')")
    @GetMapping
    public ResponseEntity<?> getSummaryReport(@RequestParam(required = false) LocalDate startDate,
                                              @RequestParam(required = false) LocalDate endDate,
                                              @RequestParam(required = false) YearMonth startMonth,
                                              @RequestParam(required = false) YearMonth endMonth) {
        return ResponseEntity.ok(summaryReportService.getStatistics(startDate, endDate,
                startMonth, endMonth));
    }
}
