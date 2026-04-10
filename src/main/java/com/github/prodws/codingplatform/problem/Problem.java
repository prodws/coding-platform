package com.github.prodws.codingplatform.problem;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "problems")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "problem_type", discriminatorType = DiscriminatorType.STRING)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
@Getter
public abstract class Problem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String title;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProblemDifficulty difficulty;

    @OneToMany(mappedBy = "problem", cascade = CascadeType.ALL)
    @Getter(AccessLevel.NONE)
    @ToString.Exclude
    private final List<ProblemFile> files = new ArrayList<>();

    public void addFile(String filePath, String fileName, FileRole fileRole) {
        checkFileRoleAllowedForType(fileRole);
        files.add(new ProblemFile(this, filePath, fileName, fileRole));
    }

    public List<ProblemFile> getFiles() {
        return Collections.unmodifiableList(files);
    }

    protected void checkFileRoleAllowedForType(FileRole fileRole) {}

    @PrePersist
    @PreUpdate
    private void validate() {
        validateFiles();
    }

    protected Problem(String title, ProblemDifficulty difficulty) {
        this.title = title;
        this.difficulty = difficulty;
        this.createdAt = LocalDateTime.now();
    }

    protected void validateFiles() {
        boolean hasDescription = files.stream()
                .anyMatch(f -> f.getFileRole() == FileRole.DESCRIPTION);
        if (!hasDescription) {
            throw new IllegalStateException(
                    "Problem '" + title + "' must have a DESCRIPTION file"
            );
        }
    }
}
