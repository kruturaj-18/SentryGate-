package com.sentrygate.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class RepositoryRuleDto {
    private Long id;
    private Long repositoryId;
    private String ruleCode;
    private String ruleName;
    private Boolean enabled;
    private String severity;
    private Map<String, Object> configuration;
}
