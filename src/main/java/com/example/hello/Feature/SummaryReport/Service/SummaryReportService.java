package com.example.hello.Feature.SummaryReport.Service;

import com.example.hello.Entity.SummaryReport;
import com.example.hello.Feature.SummaryReport.Repository.SummaryReportRepository;
import com.example.hello.Feature.SummaryReport.dto.SummaryReportDTO;
import com.example.hello.Infrastructure.Common.Constant.StringApplication;
import com.example.hello.Infrastructure.Common.dto.Response;
import com.example.hello.Mapper.SummaryReportMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SummaryReportService {
    SummaryReportRepository summaryReportRepository;
    SummaryReportMapper summaryReportMapper;

    @Transactional(readOnly = true)
    public Response<List<SummaryReportDTO>> getStatistics(LocalDate startDay, LocalDate endDay,
                                                          YearMonth startMonth, YearMonth endMonth) {
        List<SummaryReport> summaryReports = Collections.emptyList();

        if (startDay != null && endDay != null) {
            log.info("Find summary report by startDay({}) and endDay({})", startDay, endDay);
            summaryReports = summaryReportRepository.findByReportDateBetween(startDay, endDay);
        }
        else if (startMonth != null && endMonth != null) {
            log.info("Find summary report by startMonth({}) and endMonth({})", startMonth, endMonth);
            LocalDate fromDate = startMonth.atDay(1);
            LocalDate toDate = endMonth.atEndOfMonth();

            var result = summaryReportRepository.findByReportDateBetween(fromDate, toDate)
                    .stream()
                    .collect(Collectors.groupingBy(summaryReport ->
                            YearMonth.from(summaryReport.getReportDate())));
            summaryReports = result.values()
                    .stream()
                    .map(summaryReport -> {
                        var current = new SummaryReport();
                        current.setReportDate(summaryReport.getFirst().getReportDate());
                        summaryReport.forEach(current::accumulate);
                        return current;
                    })
                    .toList();
        }

        return new Response<>(
                true,
                StringApplication.FIELD.SUCCESS,
                summaryReports.stream()
                        .map(summaryReportMapper::toSummaryReportDTO)
                        .toList()
                );
    }
}
