package com.github.prodws.codingplatform.submission;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;

@Service
@Slf4j
public class SubmissionExecutor {
    private final WorkspaceManager workspaceManager;
    private final ProcessRunner processRunner;

    public SubmissionExecutor(WorkspaceManager workspaceManager,
                              ProcessRunner processRunner) {
        this.workspaceManager = workspaceManager;
        this.processRunner = processRunner;
    }

    public RawExecutionResult execute(SubmissionRequest request) {
        Path workspace = null;
        try {
            workspace = workspaceManager.createWorkspace(request.files());
            return processRunner.run(workspace);
        } catch (IOException e) {
            return new RawExecutionResult(-1, "", "Workspace error: " + e.getMessage(), false);
        } finally {
            workspaceManager.cleanup(workspace);
        }
    }
}