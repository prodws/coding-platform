package com.github.prodws.codingplatform.submission;

public record ExecutionResult(
        ExecutionStatus status,
        String stdout,
        String stderr,
        boolean passed
) {
}