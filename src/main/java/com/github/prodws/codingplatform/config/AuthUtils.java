package com.github.prodws.codingplatform.config;

import org.springframework.security.core.Authentication;

public class AuthUtils {

    private AuthUtils() {}

    public static Long extractUserId(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("Unauthorized: Authentication token is missing or invalid");
        }
        return Long.parseLong(auth.getName());
    }
}