package com.example.bankdb.service;

import com.example.bankdb.exception.UserNotFoundException;
import com.example.bankdb.model.dto.SystemStatisticsDto;
import com.example.bankdb.model.dto.UserProfileDto;
import com.example.bankdb.model.entity.User;
import com.example.bankdb.repository.BankAccountRepository;
import com.example.bankdb.repository.TransactionRepository;
import com.example.bankdb.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final BankAccountRepository bankAccountRepository;
    private final TransactionRepository transactionRepository;

    public List<UserProfileDto> getAllUsers() {
        log.info("Fetching all users");
        return userRepository.findAll().stream()
                .map(this::mapToUserProfileDto)
                .toList();
    }

    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("User with ID " + userId + " not found");
        }
        log.info("Deleting user with id: {}", userId);
        userRepository.deleteById(userId);
        log.info("User with id: {} deleted successfully", userId);
    }

    public SystemStatisticsDto getSystemStatistics() {
        long totalUsers = userRepository.count();
        long totalAccounts = bankAccountRepository.count();
        long totalTransactions = transactionRepository.count();

        return new SystemStatisticsDto(totalUsers, totalAccounts, totalTransactions);
    }

    private UserProfileDto mapToUserProfileDto(User user) {
        return new UserProfileDto(
                user.getUsername(),
                user.getEmail(),
                user.getRole().name()
        );
    }
}
