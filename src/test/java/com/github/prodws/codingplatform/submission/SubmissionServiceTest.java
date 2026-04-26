package com.github.prodws.codingplatform.submission;

import com.github.prodws.codingplatform.problem.CodingProblem;
import com.github.prodws.codingplatform.problem.ProblemDifficulty;
import com.github.prodws.codingplatform.problem.ProblemService;
import com.github.prodws.codingplatform.problem.ProblemRepository;
import com.github.prodws.codingplatform.user.User;
import com.github.prodws.codingplatform.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class SubmissionServiceTest {

    @Mock
    private ProblemService problemService;
    @Mock
    private SubmissionExecutor submissionExecutor;
    @Mock
    private SubmissionEvaluator evaluator;
    @Mock
    private SubmissionRequestFactory requestFactory;
    @Mock
    private SolutionCodeValidator validator;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ProblemRepository problemRepository;
    @Mock
    private SubmissionRepository submissionRepository;

    private SubmissionService submissionService;

    @BeforeEach
    void setUp() {
        submissionService = new SubmissionService(
                problemService, submissionExecutor, evaluator, requestFactory, validator,
                userRepository, problemRepository, submissionRepository
        );
    }

    @Test
    void submitSolution_successfulExecution() throws IOException {
        Long problemId = 1L;
        String solutionCode = "class Solution {}";
        CodingProblem problem = new CodingProblem("Test Problem", ProblemDifficulty.EASY);
        SubmissionRequest request = new SubmissionRequest(problemId, Map.of("Solution.java", solutionCode));
        RawExecutionResult rawResult = new RawExecutionResult(0, "ok", "", false);

        when(problemService.getProblemById(problemId)).thenReturn(problem);
        when(requestFactory.build(problem, solutionCode)).thenReturn(request);
        when(submissionExecutor.execute(request)).thenReturn(rawResult);
        when(evaluator.interpretResult(rawResult)).thenReturn(ExecutionStatus.PASSED);

        ExecutionResult result = submissionService.submitSolution(problemId, solutionCode);

        assertEquals(ExecutionStatus.PASSED, result.status());
        assertEquals("ok", result.stdout());
        assertEquals("", result.stderr());
        assertTrue(result.passed());

        verify(validator).validate(solutionCode);
        verify(problemService).getProblemById(problemId);
        verify(requestFactory).build(problem, solutionCode);
        verify(submissionExecutor).execute(request);
        verify(evaluator).interpretResult(rawResult);
    }

    @Test
    void submitSolution_failedExecution() throws IOException {
        Long problemId = 1L;
        String solutionCode = "class Solution { /* compile error */ }";
        CodingProblem problem = new CodingProblem("Test Problem", ProblemDifficulty.EASY);
        SubmissionRequest request = new SubmissionRequest(problemId, Map.of("Solution.java", solutionCode));
        RawExecutionResult rawResult = new RawExecutionResult(1, "", "error", false);

        when(problemService.getProblemById(problemId)).thenReturn(problem);
        when(requestFactory.build(problem, solutionCode)).thenReturn(request);
        when(submissionExecutor.execute(request)).thenReturn(rawResult);
        when(evaluator.interpretResult(rawResult)).thenReturn(ExecutionStatus.COMPILE_ERROR);

        ExecutionResult result = submissionService.submitSolution(problemId, solutionCode);

        assertEquals(ExecutionStatus.COMPILE_ERROR, result.status());
        assertEquals("", result.stdout());
        assertEquals("error", result.stderr());
        assertFalse(result.passed());
    }

    @Test
    void submitSolution_validationFails() {
        Long problemId = 1L;
        String solutionCode = "invalid code";

        doThrow(new IllegalArgumentException("Invalid code")).when(validator).validate(solutionCode);

        ExecutionResult result = submissionService.submitSolution(problemId, solutionCode);

        assertEquals(ExecutionStatus.INVALID_SUBMISSION, result.status());
        assertEquals("Invalid code", result.stderr());
        assertFalse(result.passed());

        verify(validator).validate(solutionCode);
        verifyNoInteractions(problemService, submissionExecutor, evaluator, requestFactory);
    }

    @Test
    void submitSolution_throwsIllegalArgumentException() {
        Long problemId = 1L;
        String solutionCode = "valid code";
        
        when(problemService.getProblemById(problemId)).thenThrow(new IllegalArgumentException("Invalid problem ID"));

        ExecutionResult result = submissionService.submitSolution(problemId, solutionCode);

        assertEquals(ExecutionStatus.INVALID_SUBMISSION, result.status());
        assertEquals("Invalid problem ID", result.stderr());
        assertFalse(result.passed());
        
        verify(validator).validate(solutionCode);
        verifyNoInteractions(submissionExecutor, evaluator, requestFactory);
    }

    @Test
    void submitSolution_throwsSystemError() throws IOException {
        Long problemId = 1L;
        String solutionCode = "class Solution {}";
        CodingProblem problem = new CodingProblem("Test Problem", ProblemDifficulty.EASY);
        
        when(problemService.getProblemById(problemId)).thenReturn(problem);
        when(requestFactory.build(problem, solutionCode)).thenThrow(new RuntimeException("Docker error"));

        ExecutionResult result = submissionService.submitSolution(problemId, solutionCode);

        assertEquals(ExecutionStatus.SYSTEM_ERROR, result.status());
        assertTrue(result.stderr().contains("Docker error"));
        assertFalse(result.passed());
    }

    @Test
    void saveSubmission_success() {
        Long userId = 1L;
        Long problemId = 2L;
        String code = "class Solution {}";
        ExecutionResult result = new ExecutionResult(ExecutionStatus.PASSED, "ok", "", true);

        User user = mock(User.class);
        CodingProblem problem = new CodingProblem("Test Problem", ProblemDifficulty.EASY);
        Submission expectedSubmission = Submission.builder().build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(problemRepository.findById(problemId)).thenReturn(Optional.of(problem));
        when(submissionRepository.save(any(Submission.class))).thenReturn(expectedSubmission);

        Submission actualSubmission = submissionService.saveSubmission(userId, problemId, code, result);

        assertEquals(expectedSubmission, actualSubmission);
        verify(userRepository).findById(userId);
        verify(problemRepository).findById(problemId);
        verify(submissionRepository).save(any(Submission.class));
    }

    @Test
    void saveSubmission_userNotFound_throwsException() {
        Long userId = 1L;
        Long problemId = 2L;
        String code = "class Solution {}";
        ExecutionResult result = new ExecutionResult(ExecutionStatus.PASSED, "ok", "", true);

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            submissionService.saveSubmission(userId, problemId, code, result)
        );

        assertEquals("User not found", exception.getMessage());
        verifyNoInteractions(problemRepository, submissionRepository);
    }

    @Test
    void saveSubmission_problemNotFound_throwsException() {
        Long userId = 1L;
        Long problemId = 2L;
        String code = "class Solution {}";
        ExecutionResult result = new ExecutionResult(ExecutionStatus.PASSED, "ok", "", true);
        User user = mock(User.class);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(problemRepository.findById(problemId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            submissionService.saveSubmission(userId, problemId, code, result)
        );

        assertEquals("Problem not found", exception.getMessage());
        verifyNoInteractions(submissionRepository);
    }
}
