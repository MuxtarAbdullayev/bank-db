package com.example.bankdb.service;

import com.example.bankdb.exception.IncorrectPasswordException;
import com.example.bankdb.exception.InvalidCredentialsException;
import com.example.bankdb.exception.UserAlreadyExistsException;
import com.example.bankdb.exception.UserNotFoundException;
import com.example.bankdb.model.dto.ChangePasswordRequest;
import com.example.bankdb.model.dto.LoginRequest;
import com.example.bankdb.model.dto.RegisterRequest;
import com.example.bankdb.model.dto.UserProfileDto;
import com.example.bankdb.model.entity.enums.Role;
import com.example.bankdb.model.entity.User;
import com.example.bankdb.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public String register(RegisterRequest request) {
        String username = request.getUsername();
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            log.warn("Registration failed: Username already exists: {}", username);
            throw new UserAlreadyExistsException("Username already exists!");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration failed: Email already exists: {}", request.getEmail());
            throw new UserAlreadyExistsException("Email already exists!");
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.USER);

        userRepository.save(user);
        log.info("New user registered: {}", username);
        return "User registered successfully...";
    }

    public String login(LoginRequest request) {
        Optional<User> userOpt = userRepository.findByUsername(request.getIdentifier());

        if (userOpt.isPresent() && userOpt.get().getPassword().equals(request.getPassword())) {
            log.info("Login successful for user: {}", request.getIdentifier());
            return "Login successful." + request.getIdentifier();
        } else {
            log.warn("Login failed for user: {}", request.getIdentifier());
            throw new InvalidCredentialsException("Invalid username or password");
        }
    }

    public UserProfileDto getCurrencyUserProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("User not found by email: {}", email);
                    return new UserNotFoundException("User not found.");
                });
        return new UserProfileDto(user.getUsername(), user.getEmail(), user.getRole().name());
    }

    public void changePassword(String email, ChangePasswordRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            log.warn("Password change failed: incorrect old password for user: {}", email);
            throw new IncorrectPasswordException("Old password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("Password changed successfully for user: {}", email);
    }
}
