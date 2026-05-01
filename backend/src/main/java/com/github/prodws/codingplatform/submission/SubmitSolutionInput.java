package com.github.prodws.codingplatform.submission;

public record SubmitSolutionInput(
        Long userId,
        Long problemId,
        String solutionCode
) {}