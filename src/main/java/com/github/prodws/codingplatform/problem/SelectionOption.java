package com.github.prodws.codingplatform.problem;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "selection_options")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
@Getter
public class SelectionOption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private Boolean isCorrect;

    SelectionOption(Problem problem, String content, boolean isCorrect) {
        this.problem = problem;
        this.content = content;
        this.isCorrect = isCorrect;
    }
}
