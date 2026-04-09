package com.github.prodws.codingplatform.problem.registration;

import com.github.prodws.codingplatform.problem.CodingProblem;
import com.github.prodws.codingplatform.problem.Problem;
import com.github.prodws.codingplatform.problem.ProblemType;
import org.springframework.stereotype.Component;

@Component
public class CodingProblemCreationStrategy
        implements ProblemCreationStrategy {

    @Override
    public ProblemType supports() {
        return ProblemType.CODING;
    }

    @Override
    public Problem create(CreateProblemRequest request) {
        if (request.type() != ProblemType.CODING)
            throw new IllegalStateException(
                    "Request problem type " + request.type() + " doesn't match " + ProblemType.CODING);
        if (!request.options().isEmpty()) {
            throw new IllegalArgumentException("CODING problem cannot contain options");
        }

        CodingProblem problem = new CodingProblem(request.title(), request.difficulty());
        request.files().forEach(file ->
                problem.addFile(file.path(), file.name(), file.role()));

        return problem;
    }
}
