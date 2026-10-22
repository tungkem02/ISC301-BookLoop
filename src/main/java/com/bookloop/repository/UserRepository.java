package com.bookloop.repository;

import com.bookloop.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Repository for User entity persistence operations.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by email address (case-insensitive lookup).
     */
    Optional<User> findByEmailIgnoreCase(String email);

    /**
     * Checks if an email address is already registered.
     */
    boolean existsByEmailIgnoreCase(String email);
}
