package com.example.hello.Feature.SummaryReport.dto;

import com.example.hello.Enum.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface ReportInfo {
    LocalDate getReportDate();
    BigDecimal getTotalRevenue();
    Integer getProductCount();
    Integer getOrderCount();
    OrderStatus getOrderStatus();
}
