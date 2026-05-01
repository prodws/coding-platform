package com.github.prodws.codingplatform.problem;

import com.github.prodws.codingplatform.problem.registration.CreateProblemRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProblemSeeder implements ApplicationRunner {
    private static final String PROBLEM_PATTERN = "classpath*:problems/**/problem.json";

    private final ProblemService problemService;
    private final ObjectMapper objectMapper;

    private final ResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver();

    @Override
    public void run(ApplicationArguments args) {
        loadProblemsFromResources();
    }

    private void loadProblemsFromResources() {
        try {
            Resource[] descriptors = resourceResolver.getResources(PROBLEM_PATTERN);
            for (Resource descriptor : descriptors) {
                loadSingleProblem(descriptor);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to scan problem descriptors from resources", e);
        }
    }

    private void loadSingleProblem(Resource descriptor) {
        try (InputStream in = descriptor.getInputStream()) {
            CreateProblemRequest request = objectMapper.readValue(in, CreateProblemRequest.class);
            problemService.createProblem(request);
            log.info("Seed created: {}", request.title());
        } catch (IOException e) {
            log.error("Failed to read seed: {}", descriptor.getDescription(), e);
            throw new IllegalStateException("Failed to read problem descriptor: " + descriptor.getDescription(), e);
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.warn("Skipping {}: {}", descriptor.getDescription(), e.getMessage());
        }
    }
}
