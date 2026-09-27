package com.sentrygate.validation.rules;

import com.sentrygate.validation.RuleResult;
import com.sentrygate.validation.ValidationContext;
import com.sentrygate.validation.ValidationRule;
import lombok.Builder;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Basic secret detection rule scanning file names for potential credential exposure patterns.
 * IMPORTANT: Does NOT store actual secret values, only metadata.
 */
@Component
public class SecretDetectionRule implements ValidationRule {

    private static final List<SecretPattern> SECRET_PATTERNS = List.of(
        SecretPattern.of("AWS_KEY", "AWS access key pattern", ".*aws[_-]?access[_-]?key.*", "CRITICAL"),
        SecretPattern.of("PRIVATE_KEY", "Private key file", ".*(private[_-]?key|id_rsa|id_ed25519)\\..*", "CRITICAL"),
        SecretPattern.of("ENV_FILE", "Environment secrets file", ".*\\.env(\\.local|\\.production|\\.staging)?$", "HIGH"),
        SecretPattern.of("CREDENTIALS", "Credentials file", ".*(credentials|secrets?|password)\\..*", "HIGH"),
        SecretPattern.of("KEYSTORE", "Keystore file", ".*(keystore|truststore)\\.(jks|p12|pfx)$", "CRITICAL"),
        SecretPattern.of("API_CONFIG", "API config with potential secrets", ".*(api[_-]?key|api[_-]?secret|token)\\..*", "HIGH")
    );

    @Override
    public String getRuleCode() { return "SECRET_DETECTION"; }

    @Override
    public String getRuleName() { return "Secret Detection"; }

    @Override
    public String getDefaultSeverity() { return "CRITICAL"; }

    @Override
    public RuleResult validate(ValidationContext context) {
        List<String> changedFiles = context.changedFileNames();
        List<String> findings = new ArrayList<>();

        for (String file : changedFiles) {
            String lowerFile = file.toLowerCase();
            for (SecretPattern pattern : SECRET_PATTERNS) {
                if (lowerFile.matches(pattern.getRegex())) {
                    // Log finding WITHOUT the actual content
                    findings.add("[REDACTED] Possible " + pattern.getDescription() + " in: " + file);
                }
            }
        }

        if (findings.isEmpty()) {
            return RuleResult.pass(getRuleCode(), getRuleName(), getDefaultSeverity(),
                    "No potential secret files detected in the changeset.");
        } else {
            String msg = "Potential secrets detected. Review required before merging.\n" +
                    String.join("\n", findings);
            return RuleResult.builder()
                    .ruleCode(getRuleCode())
                    .ruleName(getRuleName())
                    .status("FAIL")
                    .severity("CRITICAL")
                    .message(msg)
                    .details(Map.of("findings", findings, "note", "Values are NOT stored per security policy"))
                    .build();
        }
    }

    @Getter
    @Builder
    private static class SecretPattern {
        private final String code;
        private final String description;
        private final String regex;
        private final String severity;

        static SecretPattern of(String code, String desc, String regex, String severity) {
            return SecretPattern.builder().code(code).description(desc).regex(regex).severity(severity).build();
        }
    }
}
