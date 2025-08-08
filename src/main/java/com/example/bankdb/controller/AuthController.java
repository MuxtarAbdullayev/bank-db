package com.example.bankdb.controller;

import com.example.bankdb.model.dto.AuthResponse;
import com.example.bankdb.model.dto.JwtResponse;
import com.example.bankdb.model.dto.LoginRequest;
import com.example.bankdb.model.dto.RegisterRequest;
import com.example.bankdb.model.entity.RefreshToken;
import com.example.bankdb.model.entity.enums.Role;
import com.example.bankdb.model.entity.User;
import com.example.bankdb.repository.UserRepository;
import com.example.bankdb.service.JwtService;
import com.example.bankdb.service.RefreshTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Auth management APIs")
public class AuthController {

    private final AuthenticationManager authManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user",
            description = "Creates a new user account with the provided username, email, and password. Returns a JWT token upon successful registration.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User registered successfully",
                    content = @Content(schema = @Schema(implementation = JwtResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<?> register(@RequestBody @Valid RegisterRequest request) {
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setUsername(request.getUsername());
        user.setRole(Role.USER);
        userRepository.save(user);

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = refreshTokenService.createOrUpdateRefreshToken(user).getToken();

        return ResponseEntity.ok(new AuthResponse(accessToken, refreshToken));
    }


    @Operation(
            summary = "Refresh JWT tokens",
            description = "Accepts a refresh token in the Authorization header and returns new access and refresh tokens."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tokens refreshed successfully",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token")
    })
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@RequestHeader("Authorization") String header) {
        log.info("Refresh endpoint called with header: {}", header); // bunu əlavə et
        String token = jwtService.cleanToken(header);

        RefreshToken oldRefreshToken = refreshTokenService.getValidRefreshToken(token);
        User user = oldRefreshToken.getUser();

        refreshTokenService.deleteRefreshToken(oldRefreshToken);

        String newAccessToken = jwtService.generateAccessToken(user);
        RefreshToken newRefreshToken = refreshTokenService.createOrUpdateRefreshToken(user);

        return ResponseEntity.ok(new AuthResponse(newAccessToken, newRefreshToken.getToken()));
    }


    @PostMapping("/login")
    @Operation(summary = "Authenticate user and return JWT",
            description = "Validates user credentials (email and password) and returns a JWT token if authentication is successful."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Authentication successful",
                    content = @Content(schema = @Schema(implementation = JwtResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid email or password")
    })
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getIdentifier(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getIdentifier())
                .or(() -> userRepository.findByUsername(request.getIdentifier()))
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = refreshTokenService.createOrUpdateRefreshToken(user).getToken();

        return ResponseEntity.ok(new AuthResponse(accessToken, refreshToken));
    }
}
