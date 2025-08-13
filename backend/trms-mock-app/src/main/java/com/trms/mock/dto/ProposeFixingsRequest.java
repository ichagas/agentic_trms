package com.trms.mock.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotEmpty;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProposeFixingsRequest {
    
    @NotEmpty(message = "Instrument IDs are required")
    private List<String> instrumentIds;
    
    private LocalDate fixingDate;
    private String indexName;
    private String source;
    private Boolean autoApprove;
}