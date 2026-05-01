package com.github.prodws.codingplatform.submission;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class WorkspaceManagerTest {

    private WorkspaceManager workspaceManager;

    @BeforeEach
    void setUp() {
        workspaceManager = new WorkspaceManager();
    }

    @Test
    void createWorkspace_createsFilesAndDirectories() throws IOException {
        String content1 = "class Solution {}";
        String content2 = "hello world";
        
        Map<String, String> files = Map.of(
                "Solution.java", content1,
                "nested/folder/data.txt", content2
        );

        Path workspace = workspaceManager.createWorkspace(files);

        assertTrue(Files.exists(workspace));
        assertTrue(Files.isDirectory(workspace));

        Path file1 = workspace.resolve("Solution.java");
        assertTrue(Files.exists(file1));
        assertEquals(content1, Files.readString(file1));

        Path file2 = workspace.resolve("nested/folder/data.txt");
        assertTrue(Files.exists(file2));
        assertEquals(content2, Files.readString(file2));

        workspaceManager.cleanup(workspace);
    }

    @Test
    void cleanup_deletesWorkspaceAndContents() throws IOException {
        Map<String, String> files = Map.of(
                "Solution.java", "class Solution {}",
                "nested/folder/data.txt", "hello world"
        );

        Path workspace = workspaceManager.createWorkspace(files);

        assertTrue(Files.exists(workspace));
        
        workspaceManager.cleanup(workspace);

        assertFalse(Files.exists(workspace));
    }

    @Test
    void cleanup_handlesNullGracefully() {
        workspaceManager.cleanup(null);
    }

    @Test
    @SuppressWarnings("resource")
    void cleanup_handlesIOExceptionOnWalk() {
        Path mockPath = mock(Path.class);
        
        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.walk(mockPath)).thenThrow(new IOException("Cannot walk directory"));
            
            workspaceManager.cleanup(mockPath);
            
            mockedFiles.verify(() -> Files.walk(mockPath));
        }
    }

    @Test
    @SuppressWarnings("resource")
    void cleanup_handlesIOExceptionOnDelete() {
        Path mockPath = mock(Path.class);
        Path childPath = mock(Path.class);
        
        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.walk(mockPath)).thenAnswer(inv -> Stream.of(childPath, mockPath));
            
            mockedFiles.when(() -> Files.delete(childPath)).thenThrow(new IOException("Cannot delete file"));
            mockedFiles.when(() -> Files.delete(mockPath)).thenAnswer(invocation -> null);
            
            workspaceManager.cleanup(mockPath);

            mockedFiles.verify(() -> Files.walk(mockPath));
            mockedFiles.verify(() -> Files.delete(childPath));
            mockedFiles.verify(() -> Files.delete(mockPath));
        }
    }
}
