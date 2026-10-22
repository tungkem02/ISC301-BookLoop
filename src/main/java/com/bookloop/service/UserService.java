package com.bookloop.service;

import com.bookloop.dto.ProfileUpdateForm;
import com.bookloop.dto.RegistrationForm;
import com.bookloop.entity.Role;
import com.bookloop.entity.User;
import com.bookloop.exception.DuplicateEmailException;
import com.bookloop.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Handles business logic for user account operations.
 * Role assignment is always done here — never trusted from browser input.
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registers a new student account.
     * Hashes the password with BCrypt and assigns the STUDENT role.
     *
     * @throws DuplicateEmailException if the email address is already in use
     */
    @Transactional
    public User register(RegistrationForm form) {
        String normalizedEmail = form.getEmail().toLowerCase().trim();

        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new DuplicateEmailException("An account with that email address already exists.");
        }

        User user = new User();
        user.setFullName(form.getFullName());
        user.setEmail(normalizedEmail);
        // Hash the password — plaintext is never stored
        user.setPassword(passwordEncoder.encode(form.getPassword()));
        user.setUniversity(form.getUniversity());
        user.setCampus(form.getCampus());
        user.setPhone(form.getPhone());
        // Role is always set by the application, never by the browser
        user.setRole(Role.STUDENT);

        return userRepository.save(user);
    }

    /**
     * Retrieves a user by their ID.
     */
    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * Retrieves a user by their email address.
     */
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email);
    }

    /**
     * Updates only the permitted profile fields.
     * Email, password, and role are intentionally excluded from this operation.
     */
    @Transactional
    public User updateProfile(Long userId, ProfileUpdateForm form) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        user.setFullName(form.getFullName());
        user.setUniversity(form.getUniversity());
        user.setCampus(form.getCampus());
        user.setPhone(form.getPhone());
        user.setAvatarUrl(form.getAvatarUrl());

        return userRepository.save(user);
    }
}
