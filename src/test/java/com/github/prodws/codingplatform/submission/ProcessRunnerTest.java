package com.github.prodws.codingplatform.submission;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class ProcessRunnerTest {

    private ProcessRunner processRunner;

    @BeforeEach
    void setUp() {
        processRunner = new ProcessRunner();
        ReflectionTestUtils.setField(processRunner, "timeoutSeconds", 1L);
    }

    @Test
    void run_successfulExecution() {
        Path workspace = Path.of("/tmp/workspace");
        
        try (MockedConstruction<ProcessBuilder> ignored = mockConstruction(ProcessBuilder.class, (mock, context) -> {
            Process mockProcess = mock(Process.class);
            when(mock.start()).thenReturn(mockProcess);
            when(mockProcess.waitFor(1L, TimeUnit.SECONDS)).thenReturn(true);
            when(mockProcess.exitValue()).thenReturn(0);
            when(mockProcess.getInputStream()).thenReturn(new ByteArrayInputStream("Success output".getBytes()));
            when(mockProcess.getErrorStream()).thenReturn(new ByteArrayInputStream("".getBytes()));
        })) {
            
            RawExecutionResult result = processRunner.run(workspace);
            
            assertEquals(0, result.exitCode());
            assertEquals("Success output", result.stdout());
            assertEquals("", result.stderr());
            assertFalse(result.timeout());
        }
    }

    @Test
    void run_executionFailsWithExitCode() {
        Path workspace = Path.of("/tmp/workspace");

        try (MockedConstruction<ProcessBuilder> ignored = mockConstruction(ProcessBuilder.class, (mock, context) -> {
            Process mockProcess = mock(Process.class);
            when(mock.start()).thenReturn(mockProcess);
            when(mockProcess.waitFor(1L, TimeUnit.SECONDS)).thenReturn(true);
            when(mockProcess.exitValue()).thenReturn(1);
            when(mockProcess.getInputStream()).thenReturn(new ByteArrayInputStream("".getBytes()));
            when(mockProcess.getErrorStream()).thenReturn(new ByteArrayInputStream("Compilation Error".getBytes()));
        })) {

            RawExecutionResult result = processRunner.run(workspace);

            assertEquals(1, result.exitCode());
            assertEquals("", result.stdout());
            assertEquals("Compilation Error", result.stderr());
            assertFalse(result.timeout());
        }
    }

    @Test
    void run_executionTimeouts() {
        Path workspace = Path.of("/tmp/workspace");

        try (MockedConstruction<ProcessBuilder> ignored = mockConstruction(ProcessBuilder.class, (mock, context) -> {
            Process mockProcess = mock(Process.class);
            when(mock.start()).thenReturn(mockProcess);
            when(mockProcess.waitFor(1L, TimeUnit.SECONDS)).thenReturn(false);
        })) {

            RawExecutionResult result = processRunner.run(workspace);

            assertEquals(-1, result.exitCode());
            assertEquals("", result.stdout());
            assertEquals("Execution timed out", result.stderr());
            assertTrue(result.timeout());
        }
    }
    
    @Test
    void run_handlesIoException() {
        Path workspace = Path.of("/tmp/workspace");

        try (MockedConstruction<ProcessBuilder> ignored = mockConstruction(ProcessBuilder.class, (mock, context) -> 
            when(mock.start()).thenThrow(new IOException("Docker not found"))
        )) {

            RawExecutionResult result = processRunner.run(workspace);

            assertEquals(-1, result.exitCode());
            assertEquals("", result.stdout());
            assertEquals("IO error: Docker not found", result.stderr());
            assertFalse(result.timeout());
        }
    }

    @Test
    void run_handlesInterruptedException() {
        Path workspace = Path.of("/tmp/workspace");

        try (MockedConstruction<ProcessBuilder> ignored = mockConstruction(ProcessBuilder.class, (mock, context) -> {
            Process mockProcess = mock(Process.class);
            when(mock.start()).thenReturn(mockProcess);
            when(mockProcess.waitFor(1L, TimeUnit.SECONDS)).thenThrow(new InterruptedException("Interrupted"));
        })) {

            RawExecutionResult result = processRunner.run(workspace);

            assertEquals(-1, result.exitCode());
            assertEquals("", result.stdout());
            assertEquals("Execution interrupted", result.stderr());
            assertFalse(result.timeout());
            assertTrue(Thread.currentThread().isInterrupted());
            
            boolean wasInterrupted = Thread.interrupted();
            assertTrue(wasInterrupted);
        }
    }
}
