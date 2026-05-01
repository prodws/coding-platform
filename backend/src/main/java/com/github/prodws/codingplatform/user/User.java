package com.github.prodws.codingplatform.user;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Represents a registered user account.
 * Role defaults to PLAYER and totalPoints to 0 on creation.
 */
// persistence
@Entity
@Table(name = "users")
// lombok
@Getter
@Setter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED) // used by Hibernate
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    @Email
    private String email;

    @Column(nullable = false, length = 255)
    private String passwordHash;

    @Column(nullable = false)
    private Long totalPoints;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public User(String username, String email, String passwordHash) {
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.totalPoints = 0L;
        this.role = Role.PLAYER;
        this.createdAt = LocalDateTime.now();
    }
}
