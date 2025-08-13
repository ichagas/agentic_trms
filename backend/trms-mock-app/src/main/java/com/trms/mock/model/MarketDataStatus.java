package com.trms.mock.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarketDataStatus {
    
    @NotBlank(message = "Feed type is required")
    private String feedType;
    
    @NotNull(message = "Expected count is required")
    private Integer expected;
    
    @NotNull(message = "Received count is required")
    private Integer received;
    
    @NotNull(message = "Missing count is required")
    private Integer missing;
    
    @NotNull(message = "Complete status is required")
    private Boolean complete;
    
    private LocalDateTime lastUpdate;
    private LocalDateTime cutoffTime;
    private List<String> missingItems;
    private String provider;
    private FeedStatus status;
    
    public enum FeedStatus {
        HEALTHY,
        DELAYED,
        INCOMPLETE,
        FAILED,
        UNKNOWN
    }
}