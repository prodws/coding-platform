package com.github.prodws.codingplatform.submission;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class SolutionCodeValidator {
    @Value("${submission.max-code-size-kb:100}")
    private int maxCodeSizeKb;

    @Value("${submission.max-line-length:200}")
    private int maxLineLength;

    public void validate(String solutionCode) {
        if (solutionCode == null || solutionCode.isBlank()) {
            throw new IllegalArgumentException("Solution code cannot be empty");
        }

        int sizeKb = solutionCode.getBytes(StandardCharsets.UTF_8).length / 1024;
        if (sizeKb > maxCodeSizeKb) {
            throw new IllegalArgumentException(
                    String.format("Code exceeds %d KB limit (got %d KB)", maxCodeSizeKb, sizeKb)
            );
        }

        String[] lines = solutionCode.split("\n", -1);
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].length() > maxLineLength) {
                throw new IllegalArgumentException(
                        String.format("Line %d too long (max %d chars)", i + 1, maxLineLength)
                );
            }
        }

        checkSuspiciousPatterns(solutionCode);

        log.debug("Solution code validation passed");
    }

    private void checkSuspiciousPatterns(String code) {
        String[] dangerous = {
                "Runtime.getRuntime().exec",
                "ProcessBuilder",
                "System.exit",
                "System.load",
                "Unsafe",
        };

        for (String pattern : dangerous) {
            if (code.contains(pattern)) {
                throw new IllegalArgumentException(
                        "Code contains disallowed pattern: " + pattern
                );
            }
        }
    }
}
