package com.example.hello.Feature.SummaryReport.dto;

import java.time.LocalDate;
import java.util.UUID;

public interface IdAndReportDate {
    UUID getSummaryReportId();
    LocalDate getReportDate();
}
