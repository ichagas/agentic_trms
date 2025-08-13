package com.trms.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * End of Day Status DTO for TRMS legacy system integration
 */
public record EODStatus(
    @JsonProperty("isReady") boolean isReady,
    @JsonProperty("lastRunDate") String lastRunDate,
    @JsonProperty("nextRunDate") String nextRunDate,
    @JsonProperty("status") String status,
    @JsonProperty("blockers") List<String> blockers,
    @JsonProperty("completedChecks") List<String> completedChecks,
    @JsonProperty("pendingChecks") List<String> pendingChecks
) {}