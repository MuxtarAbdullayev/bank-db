package com.example.bankdb.service;

import com.example.bankdb.model.dto.LoginRequest;
import com.example.bankdb.model.dto.RegisterRequest;
import com.example.bankdb.model.entity.User;
import com.example.bankdb.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public String register(RegisterRequest request) {
        String username = request.getUsername();
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            return "Username already exists!";
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setFullName(request.getFullName());
        userRepository.save(user);
        return "User registered succesfully...";
    }

    public String login(LoginRequest request) {
        return userRepository.findByUsername(request.getUsername())
                .filter(user -> user.getPassword().equals(request.getPassword()))
                .map(user -> "Login successful." + user.getFullName())
                .orElse("Invalid username or password");
    }

}
