package com.example.hello.Feature.SummaryReport.Repository;

import com.example.hello.Entity.SummaryReport;
import com.example.hello.Feature.SummaryReport.dto.IdAndReportDate;
import com.example.hello.Feature.SummaryReport.dto.ReportInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface SummaryReportRepository extends JpaRepository<SummaryReport, UUID> {
    @Query("""
    SELECT
        cast(o.createdAt as localdate ) as reportDate,
        SUM(oi.price * oi.quantity) as totalRevenue,
        SUM(oi.quantity) as productCount,
        COUNT(o) as orderCount,
        o.orderStatus as orderStatus
    FROM OrderItem oi
    JOIN oi.order o
    WHERE o.createdAt >= :timeStart AND o.createdAt <= :timeEnd
    GROUP BY cast(o.createdAt as localdate ) , o.orderStatus
    """)
    List<ReportInfo> getStatistics(Instant timeStart, Instant timeEnd);

    @Query("""
            select sr.summaryReportId as summaryReportId, sr.reportDate as reportDate
            from SummaryReport sr
            where sr.reportDate in :reportDates
            """)
    List<IdAndReportDate> getIdAndReportDates(List<LocalDate> reportDates);

    @Query("""
            select sr
            from SummaryReport sr
            where sr.reportDate >= :reportDateBefore and sr.reportDate <= :reportDateAfter
            """)
    List<SummaryReport> findByReportDateBetween(LocalDate reportDateBefore, LocalDate reportDateAfter);
}