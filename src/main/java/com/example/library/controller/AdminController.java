package com.example.library.controller;

import com.example.library.model.Role;
import com.example.library.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Admin", description = "Admin management APIs")
public class AdminController {

    private final AdminService adminService;

    @Operation(
        summary = "Update user role",
        description = "Update the role of a specific user (ADMIN access required)",
        responses = {
            @ApiResponse(responseCode = "200", description = "User role updated successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "User not found")
        }
    )
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/users/{email}/role")
    public ResponseEntity<String> updateUserRole(
            @Parameter(description = "Email of the user to update") @PathVariable String email,
            @Parameter(description = "New role to assign to the user") @RequestParam Role newRole) {
        
        adminService.updateUserRole(email, newRole);
        return ResponseEntity.ok("User role updated successfully");
    }
}
