package com.github.prodws.codingplatform.submission;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class ProcessRunner {

    @Value("${runner.timeout-s:30}")
    private long timeoutSeconds;

    public RawExecutionResult run(Path workspace) {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "docker", "run", "--rm",
                    "-v", workspace.toAbsolutePath() + ":/workspace",
                    "java-test-runner:latest"
            );

            Process process = pb.start();
            boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);

            if (!finished) {
                process.destroyForcibly();
                return new RawExecutionResult(
                        -1,
                        "",
                        "Execution timed out",
                        true
                );
            }

            String output = new String(
                    process.getInputStream() != null ?
                            process.getInputStream().readAllBytes() :
                            new byte[0]
            );
            String error = new String(
                    process.getErrorStream() != null ?
                            process.getErrorStream().readAllBytes() :
                            new byte[0]
            );


            return new RawExecutionResult(
                    process.exitValue(),
                    output,
                    error,
                    false
            );

        } catch (IOException e) {
            log.error("IO error during process execution", e);

            return new RawExecutionResult(
                    -1,
                    "",
                    "IO error: " + e.getMessage(),
                    false
            );
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            return new RawExecutionResult(
                    -1,
                    "",
                    "Execution interrupted",
                    false
            );
        }
    }
}