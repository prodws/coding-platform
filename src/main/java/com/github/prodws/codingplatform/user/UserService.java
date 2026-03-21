package com.github.prodws.codingplatform.user;

import com.github.prodws.codingplatform.config.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public List<User> getUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("User not found"));
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User not found"));
    }

    public String login(String emailOrUsername, String password) {
        User user = userRepository.findByEmail(emailOrUsername)
                .orElseGet(() -> userRepository.findByUsername(emailOrUsername)
                        .orElseThrow(() -> new IllegalStateException("User not found")));

        if (!verifyPassword(password, user.getPasswordHash()))
            throw new IllegalStateException("Invalid credentials");

        return jwtTokenProvider.generateToken(user.getEmail());
    }

    public void addNewUser(String username, String email, String password) {
        checkIfEmailExists(email);
        checkIfUsernameExists(username);

        User user = new User(username, email, passwordEncoder.encode(password));
        userRepository.save(user);
    }

    private void checkIfEmailExists(String email) {
        if (userRepository.existsByEmail(email))
            throw new IllegalStateException("Email already taken");
    }

    private void checkIfUsernameExists(String username) {
        if (userRepository.existsByUsername(username))
            throw new IllegalStateException("Username already taken");
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id))
            throw new IllegalStateException("User not found");
        userRepository.deleteById(id);
    }

    public boolean verifyPassword(String rawPassword, String storedHash) {
        return passwordEncoder.matches(rawPassword, storedHash);
    }

    public void updateUsername(Long id, String newUsername) {
        User user = getUserById(id);
        checkIfUsernameExists(newUsername);
        user.setUsername(newUsername);
        userRepository.save(user);
    }

    public void updatePassword(Long id, String newPassword) {
        User user = getUserById(id);
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public void addPoints(Long id, Long pointsToAdd) {
        User user = getUserById(id);
        user.setTotalPoints(user.getTotalPoints() + pointsToAdd);
        userRepository.save(user);
    }
}
