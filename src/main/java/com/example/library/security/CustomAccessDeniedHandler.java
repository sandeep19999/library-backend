package com.example.library.security;

import com.example.library.exception.ResourceAlreadyExistsException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, 
                      AccessDeniedException accessDeniedException) throws IOException, ServletException {
        if (request.getAttribute("org.springframework.security.web.authentication.WebAuthenticationDetails") != null) {
            // This is an authentication failure
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized: Authentication failed");
        } else {
            // This is an access denied (403) or other exception
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden: You don't have permission to access this resource");
        }
    }
}
