package com.example.library.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * 
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        
        System.out.println("\n=== Incoming Request ===");
        System.out.println("Method: " + method);
        System.out.println("URI: " + requestURI);
        System.out.println("Headers: " + Collections.list(request.getHeaderNames())
                .stream()
                .map(name -> name + "=" + request.getHeader(name))
                .collect(Collectors.joining(", ")));
        
        // Skip JWT check for public endpoints
        if (isPublicEndpoint(requestURI)) {
            System.out.println("Allowing access to public endpoint: " + requestURI);
            filterChain.doFilter(request, response);
            return;
        }
        
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        System.out.println("Auth Header: " + authHeader);
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);
        userEmail = jwtService.extractUsername(jwt);

        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);
            
            if (jwtService.isTokenValid(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        filterChain.doFilter(request, response);
    }
    
    private boolean isPublicEndpoint(String requestURI) {
        // Debug log to see which endpoints are being checked
        System.out.println("Checking if endpoint is public: " + requestURI);
        
        // Allow all book endpoints
//        if (requestURI.startsWith("/api/books")) {
//            System.out.println("Allowing access to book endpoint: " + requestURI);
//            return true;
//        }
        
        // Allow auth and Swagger endpoints
        boolean isPublic = requestURI.startsWith("/api/auth/") ||
                         requestURI.startsWith("/v3/api-docs") || 
                         requestURI.startsWith("/swagger-ui") ||
                         requestURI.startsWith("/swagger-ui.html");
                         
        if (isPublic) {
            System.out.println("Allowing access to public endpoint: " + requestURI);
        } else {
            System.out.println("Requiring authentication for: " + requestURI);
        }
        
        return isPublic;
    }
}
