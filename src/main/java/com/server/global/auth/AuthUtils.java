package com.server.global.auth;

import com.server.auth.exception.AuthErrorCase;
import com.server.global.exception.ApplicationException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
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

    public static String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }

        // 쿠키 기반이라면 추가로 이렇게 가능
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookie.getName().equals("accessToken")) {
                    return cookie.getValue();
                }
            }
        }

        return null;
    }
}
