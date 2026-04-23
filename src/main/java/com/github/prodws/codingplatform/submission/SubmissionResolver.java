package com.github.prodws.codingplatform.submission;

import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class SubmissionResolver {
    private final SubmissionService submissionService;

    @MutationMapping
    public ExecutionResult submitSolution(
            @Argument Long problemId,
            @Argument String solutionCode
    ) {
        return submissionService.submitSolution(problemId, solutionCode);
    }
}
