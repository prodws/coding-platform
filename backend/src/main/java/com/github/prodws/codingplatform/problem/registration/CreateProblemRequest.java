package com.github.prodws.codingplatform.problem.registration;

import com.github.prodws.codingplatform.problem.ProblemDifficulty;
import com.github.prodws.codingplatform.problem.ProblemType;

import java.util.List;

public record CreateProblemRequest(
        ProblemType type,
        String title,
        ProblemDifficulty difficulty,
        List<FileInput> files,
        List<OptionInput> options
) {
    public CreateProblemRequest {
        files = files == null ? List.of() : List.copyOf(files);
        options = options == null ? List.of() : List.copyOf(options);
    }
}
