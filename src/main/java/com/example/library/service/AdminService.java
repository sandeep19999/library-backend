package com.example.library.service;

import com.example.library.exception.ResourceNotFoundException;
import com.example.library.model.Role;
import com.example.library.model.User;
import com.example.library.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminService {
    
    private final UserRepository userRepository;
    
    @Transactional
    public void updateUserRole(String email, Role newRole) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        
        // Prevent demoting the last admin
        if (user.getRole() == Role.ROLE_ADMIN && newRole != Role.ROLE_ADMIN) {
            long adminCount = userRepository.countByRole(Role.ROLE_ADMIN);
            if (adminCount <= 1) {
                throw new IllegalStateException("Cannot remove the last admin");
            }
        }
        
        user.setRole(newRole);
        userRepository.save(user);
    }
}
