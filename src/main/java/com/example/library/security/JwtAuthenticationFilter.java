package com.example.library.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.io.IOException;
import java.util.Collections;

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
        try {
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
            if (isPublicEndpoint(request)) {
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
                    // Get roles from the token claims
                    List<String> roles = jwtService.extractClaim(jwt, claims -> {
                        Object rolesClaim = claims.get("roles");
                        if (rolesClaim instanceof List) {
                            return (List<String>) rolesClaim;
                        }
                        return Collections.<String>emptyList();
                    });

                    // Convert role strings to SimpleGrantedAuthority objects
                    Collection<SimpleGrantedAuthority> authorities = roles.stream()
                            .filter(role -> role != null && !role.isEmpty())
                            .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList());

                    // Create authentication token with the extracted authorities
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            authorities
                    );
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    System.out.println("User " + userEmail + " authenticated with roles: " + authorities);
                }

                filterChain.doFilter(request, response);

            }
        }catch (AccessDeniedException e) {
            throw e; // Will be handled by GlobalExceptionHandler
        } catch (AuthenticationException e) {
            throw e; // Will be handled by GlobalExceptionHandler
        } catch (Exception e) {
            throw new ServletException("Authentication failed", e);
        }
    }
    
    private boolean isPublicEndpoint(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        
        // Debug log to see which endpoints are being checked
        System.out.println("Checking if endpoint is public: " + method + " " + requestURI);
        
        // Allow public GET book endpoints (list and view)
        if (requestURI.equals("/books") && "GET".equals(method)) {
            System.out.println("Allowing public access to list books");
            return true;
        }
        
        // Allow viewing individual books (GET /books/{id})
        if (requestURI.matches("^/books/\\d+$") && "GET".equals(method)) {
            System.out.println("Allowing public access to view book: " + requestURI);
            return true;
        }
        
        // Allow auth and Swagger endpoints
        boolean isPublic = requestURI.startsWith("/auth/") ||
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
