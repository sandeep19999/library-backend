package com.example.library.service;

import com.example.library.dto.AuthResponse;
import com.example.library.dto.LoginRequest;
import com.example.library.dto.RegisterRequest;
import com.example.library.dto.UserResponse;
import com.example.library.model.User;
import com.example.library.security.JwtService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        try {
            // Register the user
            UserResponse userResponse = userService.registerUser(request);
            
            // Get the user details directly from the database
            UserDetails userDetails = userService.loadUserByUsername(request.getUsername());
            
            // Generate JWT token with the user details
            String jwtToken = jwtService.generateToken(userDetails);
            
            // Manually create an authentication object
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
            );
            
            // Set the authentication in the security context
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            return AuthResponse.builder()
                    .accessToken(jwtToken)
                    .user(userResponse)
                    .build();
        } catch (Exception e) {
            SecurityContextHolder.clearContext();
            throw new RuntimeException("Registration failed: " + e.getMessage(), e);
        }
    }

    public AuthResponse login(LoginRequest request) {
        // Authenticate user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        // Get user details
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        
        // Generate JWT token
        String jwtToken = jwtService.generateToken(userDetails);
        
        // Get user details for response
        UserResponse userResponse = userService.getUserByUsername(userDetails.getUsername());
        
        return AuthResponse.builder()
                .accessToken(jwtToken)
                .user(userResponse)
                .build();
    }
}
