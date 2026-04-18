package com.example.hello.Feature.SummaryReport.Scheduled;

import com.example.hello.Entity.SummaryReport;
import com.example.hello.Enum.OrderStatus;
import com.example.hello.Feature.SummaryReport.Repository.SummaryReportRepository;
import com.example.hello.Feature.SummaryReport.dto.ReportInfo;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SummaryReportScheduled {
    SummaryReportRepository summaryReportRepository;

    @Scheduled(fixedRate = 5*60*1000)
    @Transactional
    public void totalReport(){
        //convert UTC time to Asia/Ho_Chi_Minh
        Instant now = Instant.now().plus(7, ChronoUnit.HOURS);
        //Summary of data for the last 30 days
        var orderReport = summaryReportRepository.getStatistics(now.minus(30, ChronoUnit.DAYS),
                now)
                .stream()
                .collect(Collectors.groupingBy(ReportInfo::getReportDate));
        var dateReport = summaryReportRepository.getIdAndReportDates(orderReport.keySet().stream().toList());
        var summaryReports = orderReport.entrySet().stream()
                .map(entry -> {
                    var value = entry.getValue();
                    var report = value.stream()
                            .collect(Collectors.groupingBy(ReportInfo::getOrderStatus));
                    var summaryReport = SummaryReport.builder()
                            .reportDate(entry.getKey())
                            .orderWaitingCount(report.getOrDefault(OrderStatus.WAITING, Collections.emptyList())
                                    .stream().mapToInt(ReportInfo::getOrderCount).sum())
                            .orderPendingCount(report.getOrDefault(OrderStatus.PENDING, Collections.emptyList())
                                    .stream().mapToInt(ReportInfo::getOrderCount).sum())
                            .orderDeliveringCount(report.getOrDefault(OrderStatus.DELIVERING, Collections.emptyList())
                                    .stream().mapToInt(ReportInfo::getOrderCount).sum())
                            .orderDeliveredCount(report.getOrDefault(OrderStatus.DELIVERED, Collections.emptyList())
                                    .stream().mapToInt(ReportInfo::getOrderCount).sum())
                            .orderCompletedCount(report.getOrDefault(OrderStatus.COMPLETED, Collections.emptyList())
                                    .stream().mapToInt(ReportInfo::getOrderCount).sum())
                            .orderCancelCount(report.getOrDefault(OrderStatus.CANCELED, Collections.emptyList())
                                    .stream().mapToInt(ReportInfo::getOrderCount).sum())
                            .totalRevenue(value.stream()
                                    .filter(reportInfo -> reportInfo.getOrderStatus() == OrderStatus.COMPLETED ||
                                            reportInfo.getOrderStatus() == OrderStatus.HAS_FEEDBACK)
                                    .map(ReportInfo::getTotalRevenue)
                                    .reduce(BigDecimal.ZERO, BigDecimal::add))
                            .productCount(value.stream()
                                    .filter(reportInfo -> reportInfo.getOrderStatus() != OrderStatus.CANCELED)
                                    .mapToInt(ReportInfo::getProductCount).sum())
                            .totalOrderCount(value.stream().mapToInt(ReportInfo::getOrderCount).sum())
                            .build();
                    dateReport.stream()
                            .filter(idAndReportDate -> idAndReportDate.getReportDate().equals(entry.getKey()))
                            .findAny()
                            .ifPresent(idAndReportDate ->
                                    summaryReport.setSummaryReportId(idAndReportDate.getSummaryReportId()));
                    return summaryReport;
                })
                .toList();
        summaryReportRepository.saveAll(summaryReports);
        log.info("Summary report save success.");
    }
}
