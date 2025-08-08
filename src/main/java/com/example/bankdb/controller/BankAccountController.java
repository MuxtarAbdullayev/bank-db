package com.example.bankdb.controller;

import com.example.bankdb.model.dto.BankAccountDto;
import com.example.bankdb.model.dto.CreateAccountRequest;
import com.example.bankdb.model.entity.BankAccount;
import com.example.bankdb.service.BankAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "BankAccount", description = "Account management APIs")
public class BankAccountController {

    private final BankAccountService bankAccountService;

    @Operation(
            summary = "Create a new bank account",
            description = "Creates a new bank account for the authenticated user based on the provided request data."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Account created successfully",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    @PostMapping("/create")
    public String createAccount(@Valid @RequestBody CreateAccountRequest request) {
        return bankAccountService.createAccount(request);
    }

    @Operation(
            summary = "Get accounts by username",
            description = "Retrieves all bank accounts that belong to a specific username."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Accounts retrieved",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = BankAccountDto.class)))),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/{username}")
    public List<BankAccountDto> getAccounts(@PathVariable String username) {
        return bankAccountService.getAccountDtosByUsername(username);
    }


    @Operation(
            summary = "Delete a bank account",
            description = "Deletes a bank account by ID. Only the account owner or an admin can perform this operation."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Account deleted successfully",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden - not authorized to delete this account"),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    @DeleteMapping("/{accountId}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isAccountOwner(authentication, #accountId)")
    public ResponseEntity<String> deleteAccount(@PathVariable Long accountId) {
        bankAccountService.deleteAccount(accountId);
        return ResponseEntity.ok("Account deleted");
    }


    @Operation(
            summary = "Update a bank account",
            description = "Updates a bank account's details. Only the account owner or an admin can perform this operation."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Account updated successfully",
                    content = @Content(schema = @Schema(implementation = BankAccount.class))),
            @ApiResponse(responseCode = "400", description = "Invalid account data"),
            @ApiResponse(responseCode = "403", description = "Forbidden - not authorized to update this account"),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    @PutMapping("/{accountId}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isAccountOwner(authentication, #accountId)")
    public ResponseEntity<?> updateAccount(@PathVariable Long accountId, @RequestBody BankAccount updatedAccount) {
        BankAccount account = bankAccountService.updateAccount(accountId, updatedAccount);
        return ResponseEntity.ok(account);
    }
}
