package com.github.prodws.codingplatform.submission;

public record RawExecutionResult(
        int exitCode,
        String stdout,
        String stderr,
        boolean timeout
) {}