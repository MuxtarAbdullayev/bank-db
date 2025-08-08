package com.example.bankdb.controller;

import com.example.bankdb.model.dto.CardDto;
import com.example.bankdb.model.dto.CreateCardRequest;
import com.example.bankdb.model.entity.Card;
import com.example.bankdb.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Card", description = "Card management APIs")
public class CardController {
    private final CardService cardService;

    @Operation(
            summary = "Create a new card",
            description = "Creates a new bank card for the given account. Only accessible by admins or account owners."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Card successfully created",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isAccountOwner(authentication, #request.accountId)")
    public ResponseEntity<String> createCard(@RequestBody CreateCardRequest request, Authentication authentication) {
        String email = authentication.getName();
        String result = cardService.createCard(request, email);
        return ResponseEntity.ok(result);
    }

    @Operation(
            summary = "Get cards by account ID",
            description = "Retrieves all cards associated with a specific account. Accessible by admins or account owners."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "List of cards returned successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = CardDto.class)))),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    @GetMapping("/by-account/{accountId}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isAccountOwner(authentication, #accountId)")
    public ResponseEntity<List<CardDto>> getCards(@PathVariable Long accountId) {
        return ResponseEntity.ok(cardService.getCardsByAccount(accountId));
    }


    @Operation(
            summary = "Delete a card",
            description = "Deletes a card by ID. Only accessible by admins or the card owner."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Card deleted successfully",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden - not authorized to delete this card"),
            @ApiResponse(responseCode = "404", description = "Card not found")
    })
    @DeleteMapping("/{cardId}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isCardOwner(authentication, #cardId)")
    public ResponseEntity<String> deleteCard(@PathVariable Long cardId) {
        cardService.deleteCard(cardId);
        return ResponseEntity.ok("Card deleted");
    }


    @Operation(
            summary = "Update a card",
            description = "Updates card details. Only accessible by admins or the card owner."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Card updated successfully",
                    content = @Content(schema = @Schema(implementation = CardDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid card data"),
            @ApiResponse(responseCode = "403", description = "Forbidden - not authorized to update this card"),
            @ApiResponse(responseCode = "404", description = "Card not found")
    })
    @PutMapping("/{cardId}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isCardOwner(authentication, #cardId)")
    public ResponseEntity<CardDto> updateCard(@PathVariable Long cardId, @RequestBody CreateCardRequest request) {
        Card updated = cardService.updateCard(cardId, request);
        CardDto dto = new CardDto();
        dto.setId(updated.getId());
        dto.setCardNumber(updated.getCardNumber());
        dto.setExpiryDate(updated.getExpiryDate());
        return ResponseEntity.ok(dto);
    }
}
