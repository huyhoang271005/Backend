package com.example.hello.Feature.SummaryReport.dto;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SummaryReportDTO {
    LocalDate reportDate;
    String reportMonth;
    BigDecimal totalRevenue;
    Integer orderWaitingCount;
    Integer orderPendingCount;
    Integer orderDeliveringCount;
    Integer orderDeliveredCount;
    Integer orderCancelCount;
    Integer orderCompletedCount;
    Integer totalOrderCount;
    Integer productCount;
}
