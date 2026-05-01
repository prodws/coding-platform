package com.github.prodws.codingplatform.submission;

import java.util.Map;

public record SubmissionRequest(
        Long problemId,
        Map<String, String> files
) {}