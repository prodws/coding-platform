package com.github.prodws.codingplatform.problem;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "problem_files", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"problem_id", "file_role"})
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
@Getter
public class ProblemFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false)
    private String filePath;

    @Column(nullable = false)
    private String fileName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FileRole fileRole;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    ProblemFile(Problem problem, String filePath, String fileName, FileRole fileRole) {
        this.problem = problem;
        this.filePath = filePath;
        this.fileName = fileName;
        this.fileRole = fileRole;
    }
}
