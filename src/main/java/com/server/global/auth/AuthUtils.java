package com.server.global.auth;

import com.server.auth.exception.AuthErrorCase;
import com.server.global.exception.ApplicationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class AuthUtils {

    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ApplicationException(AuthErrorCase.AUTH_INVALID_TOKEN);
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof Long) return (Long) principal;

        try {
            return Long.parseLong(principal.toString());
        } catch (NumberFormatException e) {
            throw new ApplicationException(AuthErrorCase.AUTH_INVALID_TOKEN);
        }
    }
}
