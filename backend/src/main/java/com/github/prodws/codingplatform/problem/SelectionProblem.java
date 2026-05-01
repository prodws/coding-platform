package com.github.prodws.codingplatform.problem;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@DiscriminatorValue("SELECTION")
@ToString(callSuper = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SelectionProblem extends Problem {

    @OneToMany(mappedBy = "problem", cascade = CascadeType.ALL)
    @OrderColumn(name = "sort_order")
    @Getter(AccessLevel.NONE)
    @ToString.Exclude
    private final List<SelectionOption> options = new ArrayList<>();

    public SelectionProblem(String title, ProblemDifficulty problemDifficulty) {
        super(title, problemDifficulty);
    }

    public void addOption(String content, boolean isCorrect) {
        options.add(new SelectionOption(this, content, isCorrect));
    }

    public List<SelectionOption> getOptions() {
        return Collections.unmodifiableList(options);
    }

    @Override
    protected void checkFileRoleAllowedForType(FileRole fileRole) {
        if (fileRole != FileRole.DESCRIPTION) {
            throw new IllegalArgumentException(
                    fileRole + " files are not valid for SELECTION problems"
            );
        }
    }

    @Override
    public ProblemType getType() {
        return ProblemType.SELECTION;
    }

}
