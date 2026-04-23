package com.github.prodws.codingplatform.submission;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubmissionExecutorTest {

    @Mock
    private WorkspaceManager workspaceManager;

    @Mock
    private ProcessRunner processRunner;

    private SubmissionExecutor executor;

    @BeforeEach
    void setUp() {
        executor = new SubmissionExecutor(workspaceManager, processRunner);
    }

    @Test
    void execute_successfulExecution() throws IOException {
        SubmissionRequest request = new SubmissionRequest(1L, Map.of("Test.java", "class Test {}"));
        Path mockWorkspacePath = Path.of("/mock/path");
        RawExecutionResult expectedResult = new RawExecutionResult(0, "Output", "", false);

        when(workspaceManager.createWorkspace(request.files())).thenReturn(mockWorkspacePath);
        when(processRunner.run(mockWorkspacePath)).thenReturn(expectedResult);

        RawExecutionResult result = executor.execute(request);

        assertEquals(expectedResult, result);
        verify(workspaceManager).createWorkspace(request.files());
        verify(processRunner).run(mockWorkspacePath);
        verify(workspaceManager).cleanup(mockWorkspacePath);
    }

    @Test
    void execute_workspaceCreationFails() throws IOException {
        SubmissionRequest request = new SubmissionRequest(1L, Map.of("Test.java", "class Test {}"));
        
        when(workspaceManager.createWorkspace(request.files())).thenThrow(new IOException("Disk full"));

        RawExecutionResult result = executor.execute(request);

        assertEquals(-1, result.exitCode());
        assertEquals("", result.stdout());
        assertEquals("Workspace error: Disk full", result.stderr());
        assertEquals(false, result.timeout());
        
        verify(workspaceManager).cleanup(null);
    }

    @Test
    void execute_cleanupCalledAfterProcessRun() throws IOException {
        SubmissionRequest request = new SubmissionRequest(1L, Map.of("Test.java", "class Test {}"));
        Path mockWorkspacePath = Path.of("/mock/path");
        RawExecutionResult expectedResult = new RawExecutionResult(1, "", "Runtime Error", false);

        when(workspaceManager.createWorkspace(request.files())).thenReturn(mockWorkspacePath);
        when(processRunner.run(mockWorkspacePath)).thenReturn(expectedResult);

        RawExecutionResult result = executor.execute(request);

        assertEquals(expectedResult, result);
        verify(workspaceManager).cleanup(mockWorkspacePath);
    }
}
