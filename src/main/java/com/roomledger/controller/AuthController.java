package com.roomledger.controller;

import com.roomledger.dto.AuthResponse;
import com.roomledger.dto.LoginRequest;
import com.roomledger.dto.RegisterRequest;
import com.roomledger.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Register and login to get a JWT token")
@SecurityRequirements   // No auth needed for these endpoints
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(
        summary = "Register a new user",
        description = "Creates a new account and returns a JWT token",
        responses = {
            @ApiResponse(responseCode = "200", description = "Registered successfully",
                content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Email already exists or validation error")
        }
    )
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest req) {
        return ResponseEntity.ok(authService.register(req));
    }

    @PostMapping("/login")
    @Operation(
        summary = "Login with email and password",
        description = "Returns a JWT token — paste it in the Authorize button above",
        responses = {
            @ApiResponse(responseCode = "200", description = "Login successful",
                content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
        }
    )
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(authService.login(req));
    }
}