package com.github.prodws.codingplatform.submission;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubmissionResolverTest {

    @Mock
    private SubmissionService submissionService;
    
    private SubmissionResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new SubmissionResolver(submissionService);
    }

    @Test
    void submitSolution_success() {
        Long problemId = 1L;
        String solutionCode = "class Solution {}";
        ExecutionResult expectedResult = new ExecutionResult(ExecutionStatus.PASSED, "ok", "", true);

        when(submissionService.submitSolution(problemId, solutionCode)).thenReturn(expectedResult);

        ExecutionResult result = resolver.submitSolution(problemId, solutionCode);

        assertEquals(expectedResult, result);
        verify(submissionService).submitSolution(problemId, solutionCode);
    }
}
