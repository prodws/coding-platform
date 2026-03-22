package com.github.prodws.codingplatform.config;

import org.springframework.security.core.Authentication;

public class AuthUtils {

    private AuthUtils() {}

    public static Long extractUserId(Authentication auth) {
        return Long.parseLong(auth.getName());
    }
}