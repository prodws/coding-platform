package com.github.prodws.codingplatform.problem.registration;

import com.github.prodws.codingplatform.problem.Problem;
import com.github.prodws.codingplatform.problem.ProblemType;

public interface ProblemCreationStrategy {
    ProblemType supports();
    Problem create(CreateProblemRequest request);
}
