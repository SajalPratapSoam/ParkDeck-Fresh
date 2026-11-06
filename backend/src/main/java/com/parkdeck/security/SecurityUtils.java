package com.parkdeck.security;

import com.parkdeck.entity.User;
import com.parkdeck.exception.ApiException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static User requireUser() {
        User user = optionalUser();
        if (user == null) {
            throw ApiException.unauthorized("Authentication required");
        }
        return user;
    }

    public static User optionalUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            return null;
        }
        return principal.getUser();
    }
}
