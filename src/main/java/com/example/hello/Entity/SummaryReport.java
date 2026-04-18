package com.example.hello.Entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "summary_report", indexes = {
        @Index(name = "idx_report_date", columnList = "report_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SummaryReport {
    @Id
    @Column(name = "summary_report_id")
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID summaryReportId;

    @Column(name = "report_date")
    LocalDate reportDate;

    @Column(name = "total_revenue")
    @Builder.Default
    BigDecimal totalRevenue = BigDecimal.ZERO;

    @Column(name = "order_waiting_count")
    @Builder.Default
    Integer orderWaitingCount = 0;

    @Column(name = "order_pending_count")
    @Builder.Default
    Integer orderPendingCount = 0;

    @Column(name = "order_delivering_count")
    @Builder.Default
    Integer orderDeliveringCount = 0;

    @Column(name = "order_delivered_count")
    @Builder.Default
    Integer orderDeliveredCount = 0;

    @Column(name = "order_cancel_count")
    @Builder.Default
    Integer orderCancelCount = 0;

    @Column(name = "order_completed_count")
    @Builder.Default
    Integer orderCompletedCount = 0;

    @Column(name = "total_order_count")
    @Builder.Default
    Integer totalOrderCount = 0;

    @Column(name = "product_count")
    @Builder.Default
    Integer productCount = 0;

    public void accumulate(SummaryReport summaryReport) {
        if(summaryReport == null) return;
        this.orderWaitingCount += summaryReport.orderWaitingCount;
        this.orderPendingCount += summaryReport.orderPendingCount;
        this.orderDeliveringCount += summaryReport.orderDeliveringCount;
        this.orderDeliveredCount += summaryReport.orderDeliveredCount;
        this.orderCompletedCount += summaryReport.orderCompletedCount;
        this.orderCancelCount += summaryReport.orderCancelCount;
        this.totalOrderCount += summaryReport.totalOrderCount;
        this.productCount += summaryReport.productCount;
        this.totalRevenue = totalRevenue.add(summaryReport.totalRevenue);
    }
}
