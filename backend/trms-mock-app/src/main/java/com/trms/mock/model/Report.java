package com.trms.mock.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Report {
    
    @NotBlank(message = "Report ID is required")
    private String reportId;
    
    @NotBlank(message = "Title is required")
    private String title;
    
    @NotNull(message = "Report type is required")
    private ReportType type;
    
    @NotNull(message = "Content is required")
    private String content;
    
    @NotNull(message = "Created date is required")
    private LocalDateTime createdDate;
    
    private String description;
    private ReportStatus status;
    private String generatedBy;
    private LocalDateTime lastModified;
    private Map<String, Object> parameters;
    private String format;
    
    public enum ReportType {
        BALANCE_REPORT,
        TRANSACTION_SUMMARY,
        EOD_SUMMARY,
        RISK_REPORT,
        COMPLIANCE_REPORT,
        MARKET_DATA_REPORT,
        PNL_REPORT
    }
    
    public enum ReportStatus {
        GENERATING,
        COMPLETED,
        FAILED,
        EXPIRED
    }
}