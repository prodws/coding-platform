package com.github.prodws.codingplatform.submission;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class SubmissionEvaluatorTest {

    private final SubmissionEvaluator evaluator = new SubmissionEvaluator();

    @Test
    void interpretResult_timeout_returnsTimeout() {
        RawExecutionResult result = new RawExecutionResult(-1, "", "", true);

        ExecutionStatus status = evaluator.interpretResult(result);

        assertThat(status).isEqualTo(ExecutionStatus.TIMEOUT);
    }

    @Test
    void interpretResult_compileErrorInStderr_returnsCompileError() {
        RawExecutionResult result = new RawExecutionResult(1, "", "error: cannot find symbol", false);

        ExecutionStatus status = evaluator.interpretResult(result);

        assertThat(status).isEqualTo(ExecutionStatus.COMPILE_ERROR);
    }

    @Test
    void interpretResult_compileErrorWithCannotFindSymbol_returnsCompileError() {
        RawExecutionResult result = new RawExecutionResult(1, "", "cannot find symbol", false);

        ExecutionStatus status = evaluator.interpretResult(result);

        assertThat(status).isEqualTo(ExecutionStatus.COMPILE_ERROR);
    }

    @Test
    void interpretResult_runtimeErrorInStdout_returnsRuntimeError() {
        RawExecutionResult result = new RawExecutionResult(1, "Exception in thread \"main\" java.lang.NullPointerException", "", false);

        ExecutionStatus status = evaluator.interpretResult(result);

        assertThat(status).isEqualTo(ExecutionStatus.RUNTIME_ERROR);
    }

    @Test
    void interpretResult_runtimeErrorInStderr_returnsTestsFailed() {
        RawExecutionResult result = new RawExecutionResult(1, "", "at java.lang.String.equals", false);

        ExecutionStatus status = evaluator.interpretResult(result);

        assertThat(status).isEqualTo(ExecutionStatus.TESTS_FAILED);
    }


    @Test
    void interpretResult_exitCodeZero_returnsPassed() {
        RawExecutionResult result = new RawExecutionResult(0, "All tests passed", "", false);

        ExecutionStatus status = evaluator.interpretResult(result);

        assertThat(status).isEqualTo(ExecutionStatus.PASSED);
    }

    @Test
    void interpretResult_exitCodeOne_returnsTestsFailed() {
        RawExecutionResult result = new RawExecutionResult(1, "Test failed", "", false);

        ExecutionStatus status = evaluator.interpretResult(result);

        assertThat(status).isEqualTo(ExecutionStatus.TESTS_FAILED);
    }


    @Test
    void interpretResult_nullStdoutAndStderr_handledGracefully() {
        RawExecutionResult result = new RawExecutionResult(0, null, null, false);

        ExecutionStatus status = evaluator.interpretResult(result);

        assertThat(status).isEqualTo(ExecutionStatus.PASSED);
    }

    @Test
    void interpretResult_emptyStdoutAndStderr_exitCodeBased() {
        RawExecutionResult result = new RawExecutionResult(1, "", "", false);

        ExecutionStatus status = evaluator.interpretResult(result);

        assertThat(status).isEqualTo(ExecutionStatus.TESTS_FAILED);
    }
}
