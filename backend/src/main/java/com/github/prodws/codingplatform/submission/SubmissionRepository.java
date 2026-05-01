package com.github.prodws.codingplatform.submission;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    List<Submission> findAllByUserId(Long userId);
    List<Submission> findAllByUserIdAndProblemId(Long userId, String problemId);
}