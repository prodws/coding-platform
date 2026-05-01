package com.github.prodws.codingplatform.user;

import com.github.prodws.codingplatform.config.AuthUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class UserResolver {

    private final UserService userService;

    @QueryMapping
    public User userByUsername(@Argument String username) {
        return userService.getUserByUsername(username);
    }

    @MutationMapping
    public User register(@Argument String username,
                         @Argument String email,
                         @Argument String password) {
        return userService.register(username, email, password);
    }

    @MutationMapping
    public String login(@Argument String emailOrUsername,
                        @Argument String password) {
        return userService.login(emailOrUsername, password);
    }

    @MutationMapping
    public User updateUsername(@Argument String newUsername, Authentication auth) {
        return userService.updateUsername( AuthUtils.extractUserId(auth), newUsername);
    }

    @MutationMapping
    public User updatePassword(@Argument String newPassword, Authentication auth) {
        return userService.updatePassword( AuthUtils.extractUserId(auth), newPassword);
    }
}