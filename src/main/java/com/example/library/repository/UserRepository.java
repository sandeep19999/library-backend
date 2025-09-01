package com.example.library.repository;

import com.example.library.model.Role;
import com.example.library.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    
    /**
     * Counts the number of users with a specific role
     * @param role The role to count users for
     * @return The number of users with the specified role
     */
    long countByRole(Role role);
}
