package com.github.prodws.codingplatform.problem;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@DiscriminatorValue("CODING")
@ToString(callSuper = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CodingProblem extends Problem {

    public CodingProblem(String title, ProblemDifficulty problemDifficulty) {
        super(title, problemDifficulty);
    }

    @Override
    protected void validateFiles() {
        Set<FileRole> present = getFiles().stream()
                .map(ProblemFile::getFileRole)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(FileRole.class)));
        Set<FileRole> missing = EnumSet.allOf(FileRole.class);
        missing.removeAll(present);
        if (!missing.isEmpty()) {
            throw new IllegalStateException(
                    "CODING problem '" + getTitle() + "' is missing required files: " + missing
            );
        }
    }
}
