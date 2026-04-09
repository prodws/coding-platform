package com.github.prodws.codingplatform.problem.registration;

import com.github.prodws.codingplatform.problem.Problem;
import com.github.prodws.codingplatform.problem.ProblemType;
import com.github.prodws.codingplatform.problem.SelectionProblem;
import org.springframework.stereotype.Component;

@Component
public class SelectionProblemCreationStrategy
        implements ProblemCreationStrategy {
    @Override
    public ProblemType supports() {
        return ProblemType.SELECTION;
    }

    @Override
    public Problem create(CreateProblemRequest request) {
        if (request.type() != ProblemType.SELECTION)
            throw new IllegalStateException(
                    "Request problem type " + request.type() + " doesn't match " + ProblemType.SELECTION);
        SelectionProblem problem = new SelectionProblem(request.title(), request.difficulty());
        request.options().forEach(option ->
                        problem.addOption(option.content(), option.isCorrect()));
        request.files().forEach(file ->
                        problem.addFile(file.path(), file.name(), file.role()));

        return problem;
    }
}
