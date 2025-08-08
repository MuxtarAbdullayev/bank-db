package com.example.bankdb.service;

import com.example.bankdb.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String input) throws UsernameNotFoundException {
        log.info("Trying to load user by input: {}", input);

        return userRepository.findByEmail(input)
                .or(() -> userRepository.findByUsername(input)) // əvvəl email, sonra username yoxla
                .orElseThrow(() -> {
                    log.error("User not found with email or username: {}", input);
                    return new UsernameNotFoundException("User not found with email or username: " + input);
                });
    }
}
