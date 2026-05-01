package com.github.prodws.codingplatform.submission;

import com.github.prodws.codingplatform.problem.CodingProblem;
import com.github.prodws.codingplatform.problem.FileRole;
import com.github.prodws.codingplatform.problem.ProblemFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubmissionRequestFactoryTest {

    @Mock
    private ResourceLoader resourceLoader;

    private SubmissionRequestFactory factory;

    @BeforeEach
    void setUp() {
        factory = new SubmissionRequestFactory(resourceLoader);
    }

    @Test
    void build_success_mapsFilesCorrectly() throws IOException {
        CodingProblem problem = mock(CodingProblem.class);
        when(problem.getId()).thenReturn(1L);

        ProblemFile templateFile = mock(ProblemFile.class);
        when(templateFile.getFileRole()).thenReturn(FileRole.TEMPLATE);
        when(templateFile.getFileName()).thenReturn("Solution.java");

        ProblemFile testFile = mock(ProblemFile.class);
        when(testFile.getFileRole()).thenReturn(FileRole.TEST);
        when(testFile.getFileName()).thenReturn("Test.java");
        when(testFile.getFilePath()).thenReturn("problems/test/test.java");

        when(problem.getFiles()).thenReturn(List.of(templateFile, testFile));

        String solutionCode = "public class Solution { }";
        String testCode = "public class Test { }";

        mockResource("problems/test/test.java", testCode);

        SubmissionRequest request = factory.build(problem, solutionCode);

        assertThat(request.problemId()).isEqualTo(1L);
        assertThat(request.files()).hasSize(2);
        assertThat(request.files().get("Solution.java")).isEqualTo(solutionCode);
        assertThat(request.files().get("Test.java")).isEqualTo(testCode);
    }

    @Test
    void build_missingTemplateFile_throwsException() {
        CodingProblem problem = mock(CodingProblem.class);

        ProblemFile testFile = mock(ProblemFile.class);
        when(testFile.getFileRole()).thenReturn(FileRole.TEST);

        when(problem.getFiles()).thenReturn(List.of(testFile));

        assertThatThrownBy(() -> factory.build(problem, "code"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Missing required file role: TEMPLATE");
    }

    @Test
    void build_missingTestFile_throwsException() {
        CodingProblem problem = mock(CodingProblem.class);

        ProblemFile templateFile = mock(ProblemFile.class);
        when(templateFile.getFileRole()).thenReturn(FileRole.TEMPLATE);

        when(problem.getFiles()).thenReturn(List.of(templateFile));

        assertThatThrownBy(() -> factory.build(problem, "code"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Missing required file role: TEST");
    }

    @Test
    void build_missingBothFiles_throwsException() {
        CodingProblem problem = mock(CodingProblem.class);
        when(problem.getFiles()).thenReturn(List.of());

        assertThatThrownBy(() -> factory.build(problem, "code"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Missing required file role: TEMPLATE");
    }

    @Test
    void build_resourceNotFound_throwsException() throws IOException {
        CodingProblem problem = mock(CodingProblem.class);

        ProblemFile templateFile = mock(ProblemFile.class);
        when(templateFile.getFileRole()).thenReturn(FileRole.TEMPLATE);

        ProblemFile testFile = mock(ProblemFile.class);
        when(testFile.getFileRole()).thenReturn(FileRole.TEST);
        when(testFile.getFilePath()).thenReturn("problems/test/test.java");

        when(problem.getFiles()).thenReturn(List.of(templateFile, testFile));

        Resource missingResource = mock(Resource.class);
        when(missingResource.getInputStream()).thenThrow(new IOException("Resource not found"));
        when(resourceLoader.getResource("classpath:problems/test/test.java")).thenReturn(missingResource);

        assertThatThrownBy(() -> factory.build(problem, "code"))
                .isInstanceOf(IOException.class)
                .hasMessage("Resource not found");
    }



    @Test
    void build_ioExceptionDuringRead_throwsException() throws IOException {
        CodingProblem problem = mock(CodingProblem.class);

        ProblemFile templateFile = mock(ProblemFile.class);
        when(templateFile.getFileRole()).thenReturn(FileRole.TEMPLATE);

        ProblemFile testFile = mock(ProblemFile.class);
        when(testFile.getFileRole()).thenReturn(FileRole.TEST);
        when(testFile.getFilePath()).thenReturn("problems/test/test.java");

        when(problem.getFiles()).thenReturn(List.of(templateFile, testFile));

        Resource faultyResource = mock(Resource.class);
        when(faultyResource.getInputStream()).thenThrow(new IOException("Read error"));
        when(resourceLoader.getResource("classpath:problems/test/test.java")).thenReturn(faultyResource);

        assertThatThrownBy(() -> factory.build(problem, "code"))
                .isInstanceOf(IOException.class)
                .hasMessage("Read error");
    }


    @Test
    void build_largeFileContent_loadsSuccessfully() throws IOException {
        CodingProblem problem = mock(CodingProblem.class);
        when(problem.getId()).thenReturn(1L);

        ProblemFile templateFile = mock(ProblemFile.class);
        when(templateFile.getFileRole()).thenReturn(FileRole.TEMPLATE);
        when(templateFile.getFileName()).thenReturn("Solution.java");

        ProblemFile testFile = mock(ProblemFile.class);
        when(testFile.getFileRole()).thenReturn(FileRole.TEST);
        when(testFile.getFileName()).thenReturn("Test.java");
        when(testFile.getFilePath()).thenReturn("problems/test/test.java");

        when(problem.getFiles()).thenReturn(List.of(templateFile, testFile));

        String largeContent = "public class Test {\n" + "  public void test() {}\n".repeat(1000) + "}";
        mockResource("problems/test/test.java", largeContent);

        SubmissionRequest request = factory.build(problem, "solution");

        assertThat(request.files().get("Test.java")).isEqualTo(largeContent);
    }

    @Test
    void build_fileWithSpecialCharacters_loadsSuccessfully() throws IOException {
        CodingProblem problem = mock(CodingProblem.class);
        when(problem.getId()).thenReturn(1L);

        ProblemFile templateFile = mock(ProblemFile.class);
        when(templateFile.getFileRole()).thenReturn(FileRole.TEMPLATE);
        when(templateFile.getFileName()).thenReturn("Solution.java");

        ProblemFile testFile = mock(ProblemFile.class);
        when(testFile.getFileRole()).thenReturn(FileRole.TEST);
        when(testFile.getFileName()).thenReturn("Test.java");
        when(testFile.getFilePath()).thenReturn("problems/test/test.java");

        when(problem.getFiles()).thenReturn(List.of(templateFile, testFile));

        String contentWithSpecialChars = "public class Test { // Comment with émojis: 🎉 }";
        mockResource("problems/test/test.java", contentWithSpecialChars);

        SubmissionRequest request = factory.build(problem, "solution");

        assertThat(request.files().get("Test.java")).isEqualTo(contentWithSpecialChars);
    }

    @Test
    void build_emptyTestFileContent_loadsSuccessfully() throws IOException {
        CodingProblem problem = mock(CodingProblem.class);
        when(problem.getId()).thenReturn(1L);

        ProblemFile templateFile = mock(ProblemFile.class);
        when(templateFile.getFileRole()).thenReturn(FileRole.TEMPLATE);
        when(templateFile.getFileName()).thenReturn("Solution.java");

        ProblemFile testFile = mock(ProblemFile.class);
        when(testFile.getFileRole()).thenReturn(FileRole.TEST);
        when(testFile.getFileName()).thenReturn("Test.java");
        when(testFile.getFilePath()).thenReturn("problems/test/test.java");

        when(problem.getFiles()).thenReturn(List.of(templateFile, testFile));

        mockResource("problems/test/test.java", "");

        SubmissionRequest request = factory.build(problem, "solution");

        assertThat(request.files().get("Test.java")).isEqualTo("");
    }

    @Test
    void build_multipleProblems_keepsFileNameMapping() throws IOException {
        CodingProblem problem = mock(CodingProblem.class);
        when(problem.getId()).thenReturn(2L);

        ProblemFile templateFile = mock(ProblemFile.class);
        when(templateFile.getFileRole()).thenReturn(FileRole.TEMPLATE);
        when(templateFile.getFileName()).thenReturn("MyCustomName.java");

        ProblemFile testFile = mock(ProblemFile.class);
        when(testFile.getFileRole()).thenReturn(FileRole.TEST);
        when(testFile.getFileName()).thenReturn("TestFile.java");
        when(testFile.getFilePath()).thenReturn("problems/custom/test.java");

        when(problem.getFiles()).thenReturn(List.of(templateFile, testFile));

        String testCode = "// test content";
        mockResource("problems/custom/test.java", testCode);

        SubmissionRequest request = factory.build(problem, "solution");

        assertThat(request.files())
                .containsEntry("MyCustomName.java", "solution")
                .containsEntry("TestFile.java", testCode);
    }

    private void mockResource(String path, String content) throws IOException {
        Resource mockResource = mock(Resource.class);
        when(mockResource.getInputStream()).thenReturn(
                new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8))
        );
        when(resourceLoader.getResource("classpath:" + path)).thenReturn(mockResource);
    }
}
