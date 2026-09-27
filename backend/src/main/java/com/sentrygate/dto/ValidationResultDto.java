package com.sentrygate.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ValidationResultDto {
    private Long id;
    private String ruleCode;
    private String ruleName;
    private String status;
    private String severity;
    private String message;
    private Map<String, Object> details;
    private OffsetDateTime createdAt;
}
