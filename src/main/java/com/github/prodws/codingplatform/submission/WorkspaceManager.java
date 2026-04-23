package com.github.prodws.codingplatform.submission;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Map;

@Service
@Slf4j
public class WorkspaceManager {

    public Path createWorkspace(Map<String, String> files) throws IOException {
        Path workspace = Files.createTempDirectory("submission-");

        for (var entry : files.entrySet()) {
            Path filePath = workspace.resolve(entry.getKey());
            Files.createDirectories(filePath.getParent());
            Files.writeString(filePath, entry.getValue());
        }

        return workspace;
    }

    public void cleanup(Path workspace) {
        if (workspace == null) return;

        try {
            Files.walk(workspace)
                    .sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            log.warn("Failed to delete file: {}", path, e);
                        }
                    });
            log.debug("Cleaned up workspace: {}", workspace);
        } catch (IOException e) {
            log.error("Failed to clean workspace: {}", workspace, e);
        }
    }
}