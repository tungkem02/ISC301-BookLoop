package com.bookloop.config;

import com.bookloop.entity.User;
import com.bookloop.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Helper component that resolves the currently authenticated User entity
 * from the Spring Security context and the database.
 *
 * Controllers use this to obtain the real User object without relying
 * on any browser-submitted identity.
 */
@Component
public class AuthenticatedUserResolver {

    private final UserRepository userRepository;

    public AuthenticatedUserResolver(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Returns the User entity for the currently authenticated principal.
     * The email is taken from the Security context (set during login).
     *
     * @throws IllegalStateException if no authenticated user is found
     */
    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            throw new IllegalStateException("No authenticated user in current context.");
        }
        String email = auth.getName(); // getName() returns the email (our username)
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalStateException(
                        "Authenticated user not found in database: " + email));
    }

    /**
     * Returns true if there is a currently authenticated (non-anonymous) user.
     */
    public boolean isAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated()
                && !auth.getPrincipal().equals("anonymousUser");
    }
}
