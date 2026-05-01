package com.github.prodws.codingplatform.submission;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SubmissionEvaluator {

    public ExecutionStatus interpretResult(RawExecutionResult result) {
        String stdout = result.stdout();
        String stderr = result.stderr();
        int exitCode = result.exitCode();

        if (result.timeout()) {
            return ExecutionStatus.TIMEOUT;
        }

        String full = (result.stdout() == null ? "" : result.stdout())
                + "\n"
                + (result.stderr() == null ? "" : result.stderr());

        if (full.contains("AssertionFailedError")
                || full.contains("Tests run:")) {
            return ExecutionStatus.TESTS_FAILED;
        }

        if (full.contains("Exception")
                || full.contains("Error")
                || full.contains("StackOverflowError")) {
            return ExecutionStatus.RUNTIME_ERROR;
        }

        if (full.contains("cannot find symbol") || full.contains("error:")) {
            return ExecutionStatus.COMPILE_ERROR;
        }

        return result.exitCode() == 0
                ? ExecutionStatus.PASSED
                : ExecutionStatus.TESTS_FAILED;
    }
}
