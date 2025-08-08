package com.example.bankdb.controller;

import com.example.bankdb.model.dto.*;
import com.example.bankdb.model.entity.Transaction;
import com.example.bankdb.model.entity.enums.TransactionType;
import com.example.bankdb.service.EmailSenderService;
import com.example.bankdb.service.TransactionService;
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
import org.springframework.web.bind.annotation.*;

import javax.security.auth.login.AccountNotFoundException;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Transaction", description = "Transaction management APIs")
public class TransactionController {

    private final TransactionService transactionService;
    private final EmailSenderService emailSenderService;


    @Operation(
            summary = "Process a generic transaction",
            description = "Processes a transaction based on the provided type in the request (e.g., deposit, withdrawal, transfer)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transaction processed",
                    content = @Content(schema = @Schema(implementation = TransactionDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    @PostMapping
    public TransactionDto process(@RequestBody TransactionRequest request) throws Exception {
        Transaction ta = transactionService.processTransaction(request);
        return transactionService.convertToDTO(ta);
    }

    @Operation(
            summary = "Transfer between accounts",
            description = "Transfers funds between two bank accounts. Sets transaction type to TRANSFER automatically."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transfer successful",
                    content = @Content(schema = @Schema(implementation = TransactionDto.class))),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    @PostMapping("/transfer")
    public TransactionDto transfer(@RequestBody TransferRequest request) throws AccountNotFoundException {
        TransactionRequest tr = new TransactionRequest();
        tr.setSourceAccountId(request.getSourceAccountId());
        tr.setDestinationAccountId(request.getDestinationAccountId());
        tr.setAmount(request.getAmount());
        tr.setDescription(request.getDescription());
        tr.setType(TransactionType.TRANSFER);

        Transaction t = transactionService.processTransaction(tr);
        return transactionService.convertToDTO(t);
    }


    @Operation(
            summary = "Card-to-card transfer",
            description = "Transfers money from one card to another. Uses a specialized method for card transactions."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Card-to-card transfer successful",
                    content = @Content(schema = @Schema(implementation = TransactionDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    @PostMapping("/card-to-card")
    public TransactionDto cardToCard(@RequestBody CardToCardRequest request) {
        TransactionRequest tr = new TransactionRequest();
        tr.setSourceCardId(request.getSourceCardId());
        tr.setDestinationCardId(request.getDestinationCardId());
        tr.setAmount(request.getAmount());
        tr.setDescription(request.getDescription());
        tr.setType(TransactionType.TRANSFER);

        Transaction t = transactionService.processCardToCardTransaction(tr);
        return transactionService.convertToDTO(t);
    }


    @Operation(
            summary = "Top-up account",
            description = "Adds money to a bank account. Sets transaction type to DEPOSIT automatically."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Top-up successful",
                    content = @Content(schema = @Schema(implementation = TransactionDto.class))),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    @PostMapping("/topup")
    public TransactionDto topUp(@RequestBody TopUpRequest request) throws AccountNotFoundException {
        TransactionRequest tr = new TransactionRequest();
        tr.setDestinationAccountId(request.getDestinationAccountId());
        tr.setAmount(request.getAmount());
        tr.setDescription(request.getDescription());
        tr.setType(TransactionType.DEPOSIT);

        Transaction t = transactionService.processTransaction(tr);
        return transactionService.convertToDTO(t);
    }


    @Operation(
            summary = "Make a payment",
            description = "Processes a withdrawal from a bank account. Sets transaction type to WITHDRAW automatically."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment processed",
                    content = @Content(schema = @Schema(implementation = TransactionDto.class))),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    @PostMapping("/payment")
    public TransactionDto payment(@RequestBody PaymentRequest request) throws AccountNotFoundException {
        TransactionRequest tr = new TransactionRequest();
        tr.setSourceAccountId(request.getSourceAccountId());
        tr.setAmount(request.getAmount());
        tr.setDescription(request.getDescription());
        tr.setType(TransactionType.WITHDRAW);

        Transaction t = transactionService.processTransaction(tr);
        return transactionService.convertToDTO(t);
    }


    @Operation(
            summary = "Get transaction history",
            description = "Retrieves the transaction history for the specified account ID."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transaction history retrieved",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = TransactionDto.class)))),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    @GetMapping("/transactions/{accountId}")
    public ResponseEntity<List<TransactionDto>> getTransactionHistory(@PathVariable Long accountId) {
        List<TransactionDto> dto = transactionService.getTransactionHistoryByAccountId(accountId);
        return ResponseEntity.ok(dto);
    }


    @Operation(
            summary = "Send transaction details via email",
            description = "Finds a transaction by ID and sends its details to the specified email address."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Email sent successfully",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "400", description = "Transaction not found or invalid email address",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "500", description = "Server error while sending email",
                    content = @Content(schema = @Schema(implementation = String.class)))
    })
    @GetMapping("/{transactionId}/send-email")
    public ResponseEntity<String> sendTransactionEmail(
            @PathVariable Long transactionId,
            @RequestParam String toEmail) {

        try {
            Transaction transaction = transactionService.getTransactionById(transactionId);

            if (transaction == null) {
                return ResponseEntity.badRequest().body("Transaction not found");
            }

            String subject = "Transaction Details - ID: " + transactionId;
            String body = buildEmailBody(transaction);

            emailSenderService.sendEmail(toEmail, subject, body);

            return ResponseEntity.ok("Email sent successfully to " + toEmail);
        } catch (AccountNotFoundException e) {
            return ResponseEntity.badRequest().body("Transaction not found or invalid");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error sending email: " + e.getMessage());
        }
    }

    private String buildEmailBody(Transaction transaction) {
        StringBuilder sb = new StringBuilder();

        sb.append("Transaction ID: ").append(transaction.getId()).append("\n");
        sb.append("Type: ").append(transaction.getTransactionType()).append("\n");
        sb.append("Amount: ").append(transaction.getAmount()).append("\n");
        sb.append("Description: ").append(transaction.getDescription()).append("\n");
        sb.append("Timestamp: ").append(transaction.getTimestamp()).append("\n");

        if (transaction.getFromAccount() != null)
            sb.append("From Account ID: ").append(transaction.getFromAccount().getId()).append("\n");

        if (transaction.getToAccount() != null)
            sb.append("To Account ID: ").append(transaction.getToAccount().getId()).append("\n");

        if (transaction.getFromCard() != null)
            sb.append("From Card ID: ").append(transaction.getFromCard().getId()).append("\n");

        if (transaction.getToCard() != null)
            sb.append("To Card ID: ").append(transaction.getToCard().getId()).append("\n");

        return sb.toString();
    }
}
