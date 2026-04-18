package com.example.hello.Mapper;

import com.example.hello.Entity.SummaryReport;
import com.example.hello.Feature.SummaryReport.dto.SummaryReportDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SummaryReportMapper {
    @Mapping(target = "reportDate", source = "reportDate")
    @Mapping(target = "reportMonth", source = "reportDate", dateFormat = "yyyy-MM")
    SummaryReportDTO toSummaryReportDTO(SummaryReport summaryReport);
}
