package com.trms.mock.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EODRunRequest {
    
    private LocalDate businessDate;
    private Boolean forceRun;
    private Boolean skipValidation;
    private String initiatedBy;
}