package com.github.prodws.codingplatform.submission;

import com.github.prodws.codingplatform.problem.CodingProblem;
import com.github.prodws.codingplatform.problem.FileRole;
import com.github.prodws.codingplatform.problem.Problem;
import com.github.prodws.codingplatform.problem.ProblemFile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class SubmissionRequestFactory {
    private final ResourceLoader resourceLoader;

    public SubmissionRequestFactory(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public SubmissionRequest build(CodingProblem problem, String solutionCode) throws IOException {
        ProblemFile templateFile = requireFile(problem, FileRole.TEMPLATE);
        ProblemFile testFile = requireFile(problem, FileRole.TEST);

        String testCode = readClasspathFile(testFile.getFilePath());

        Map<String, String> files = new LinkedHashMap<>();
        files.put(templateFile.getFileName(), solutionCode);
        files.put(testFile.getFileName(), testCode);

        return new SubmissionRequest(problem.getId(), files);
    }

    private ProblemFile requireFile(Problem problem, FileRole role) {
        return problem.getFiles().stream()
                .filter(file -> file.getFileRole() == role)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Missing required file role: " + role));
    }

    private String readClasspathFile(String path) throws IOException {
        Resource resource = resourceLoader.getResource("classpath:" + path);

        try (InputStream in = resource.getInputStream()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}