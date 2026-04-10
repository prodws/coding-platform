package com.github.prodws.codingplatform.problem;

import tools.jackson.databind.ObjectMapper;
import com.github.prodws.codingplatform.problem.registration.CreateProblemRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProblemSeederTest {

    @Mock
    private ProblemService problemService;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private Resource descriptor;

    private ProblemSeeder problemSeeder;

    @BeforeEach
    void setUp() {
        problemSeeder = new ProblemSeeder(problemService, objectMapper);
    }

    @Test
    void loadSingleProblem_readIOException_throwsIllegalStateException() throws Exception {
        InputStream in = new ByteArrayInputStream("{}".getBytes(StandardCharsets.UTF_8));
        when(descriptor.getDescription()).thenReturn("classpath:problems/dummy/problem.json");
        when(descriptor.getInputStream()).thenThrow(new IOException("broken input stream"));

        assertThatThrownBy(() ->
                ReflectionTestUtils.invokeMethod(problemSeeder, "loadSingleProblem", descriptor)
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Failed to read problem descriptor");
    }

    @Test
    void loadSingleProblem_serviceThrowsIllegalState_skipsWithoutThrowing() throws Exception {
        InputStream in = new ByteArrayInputStream("{}".getBytes(StandardCharsets.UTF_8));
        when(descriptor.getInputStream()).thenReturn(in);
        when(descriptor.getDescription()).thenReturn("classpath:problems/dummy/problem.json");

        CreateProblemRequest request = new CreateProblemRequest(
                ProblemType.CODING,
                "Dummy",
                ProblemDifficulty.EASY,
                List.of(),
                List.of()
        );

        when(objectMapper.readValue(any(InputStream.class), eq(CreateProblemRequest.class)))
                .thenReturn(request);

        doThrow(new IllegalStateException("Problem title already exists"))
                .when(problemService).createProblem(request);

        assertDoesNotThrow(() ->
                ReflectionTestUtils.invokeMethod(problemSeeder, "loadSingleProblem", descriptor)
        );

        verify(problemService).createProblem(request);
    }
}
