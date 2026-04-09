package com.github.prodws.codingplatform.problem;

import com.github.prodws.codingplatform.problem.registration.ProblemCreationStrategy;
import com.github.prodws.codingplatform.problem.registration.RegisterProblemRequest;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class ProblemService {
    private final ProblemRepository problemRepository;

    public List<Problem> getProblems() {
        return problemRepository.findAll();
    }

    public Problem getProblemById(Long id) {
        return problemRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Problem not found"));
    }

    public Problem getProblemByTitle(String title) {
        return problemRepository.findByTitle(title)
                .orElseThrow(() -> new IllegalStateException("Problem not found"));
    }

    private final Map<ProblemType, ProblemCreationStrategy> strategyByType;

    public ProblemService(ProblemRepository problemRepository, List<ProblemCreationStrategy> strategies) {
        this.problemRepository = problemRepository;

        Map<ProblemType, ProblemCreationStrategy> map = new EnumMap<>(ProblemType.class);
        for (ProblemCreationStrategy s : strategies) {
            ProblemCreationStrategy previous = map.put(s.supports(), s);
            if (previous != null) {
                throw new IllegalStateException("Duplicate strategy for type: " + s.supports());
            }
        }
        this.strategyByType = Map.copyOf(map);
    }


    @Transactional
    public Problem createProblem(RegisterProblemRequest request) {
        checkIfProblemExists(request.title());

        ProblemCreationStrategy strategy = strategyByType.get(request.type());
        if (strategy == null) {
            throw new IllegalStateException("No strategy available for type: " + request.type());
        }

        Problem problem = strategy.create(request);

        return problemRepository.save(problem);
    }

    private void checkIfProblemExists(String title) {
        if (problemRepository.existsByTitle(title))
            throw new IllegalStateException("Problem title already exists");
    }

}
