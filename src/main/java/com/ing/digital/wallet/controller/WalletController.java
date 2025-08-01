package com.ing.digital.wallet.controller;

import com.ing.digital.wallet.dto.ApprovalRequestDto;
import com.ing.digital.wallet.dto.CreateWalletRequestDto;
import com.ing.digital.wallet.dto.DepositRequestDto;
import com.ing.digital.wallet.dto.WithdrawRequestDto;
import com.ing.digital.wallet.model.Customer;
import com.ing.digital.wallet.model.Transaction;
import com.ing.digital.wallet.model.Wallet;
import com.ing.digital.wallet.service.CustomerService;
import com.ing.digital.wallet.service.TransactionService;
import com.ing.digital.wallet.service.WalletService;
import com.ing.digital.wallet.util.AuthUtil;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
@Tag(name = "Wallet API", description = "Operations related to wallet")
@SecurityRequirement(name = "Bearer Authentication")
public class WalletController {
    private final WalletService walletService;
    private final TransactionService transactionService;
    private final AuthUtil authUtil;
    private final CustomerService customerService;

    @PostMapping("/wallets/create")
    public ResponseEntity<Wallet> createWallet(@Valid @RequestBody CreateWalletRequestDto request) {
        Long customerId = request.getCustomerId();
        Customer customer = customerService.getCustomerById(customerId);
        if (!authUtil.isEmployee() && !authUtil.isSameUser(customer.getUsername())) {
            throw new AccessDeniedException("Employees can get any customer, customers can only get themselves");
        }
        String name = request.getName();
        String currency = request.getCurrency();
        boolean shop = request.isShop();
        boolean withdraw = request.isWithdraw();
        return ResponseEntity.ok(walletService.createWallet(customerId, name, currency, shop, withdraw));
    }

    @GetMapping("/wallets/{customerId}")
    public ResponseEntity<List<Wallet>> getWallets(@PathVariable Long customerId) {
        Customer customer = customerService.getCustomerById(customerId);
        if (!authUtil.isEmployee() && !authUtil.isSameUser(customer.getUsername())) {
            throw new AccessDeniedException("Employees can get any customer wallets, customers can only get their own wallets");
        }
        return ResponseEntity.ok(walletService.getWallets(customerId));
    }

    @GetMapping("/wallets/{customerId}/currency/{currency}")
    public ResponseEntity<List<Wallet>> getWalletsByCustomerAndCurrency(@PathVariable Long customerId,
                                                                        @PathVariable String currency) {
        Customer customer = customerService.getCustomerById(customerId);
        if (!authUtil.isEmployee() && !authUtil.isSameUser(customer.getUsername())) {
            throw new AccessDeniedException("Employees can get any customer wallets, customers can only get their own wallets");
        }
        return ResponseEntity.ok(walletService.getWalletsByCustomerAndCurrency(customerId, currency));
    }

    @GetMapping("/wallets/{walletId}/transactions")
    public ResponseEntity<List<Transaction>> listTransactions(@PathVariable Long walletId) {
        Wallet wallet = walletService.getWalletById(walletId);
        Customer customer = customerService.getCustomerById(wallet.getCustomer().getId());
        if (!authUtil.isEmployee() && !authUtil.isSameUser(customer.getUsername())) {
            throw new AccessDeniedException("Employees can get any customer wallet transactions, customers can only get their own wallet transactions");
        }
        List<Transaction> transactions = walletService.listTransactions(walletId);
        return ResponseEntity.ok(transactions);
    }

    @PostMapping("/transactions/deposit")
    public ResponseEntity<Transaction> deposit(@Valid @RequestBody DepositRequestDto request) {
        Long walletId = request.getWalletId();
        BigDecimal amount = request.getAmount();
        String sourceType = request.getSourceType();
        String source = request.getSource();
        Wallet wallet = walletService.getWalletById(walletId);
        Customer customer = customerService.getCustomerById(wallet.getCustomer().getId());
        if (!authUtil.isEmployee() && !authUtil.isSameUser(customer.getUsername())) {
            throw new AccessDeniedException("Employees can do any transaction, customers can only their own transactions");
        }
        Transaction tx = transactionService.deposit(walletId, amount, sourceType, source);
        return ResponseEntity.ok(tx);
    }

    @PostMapping("/transactions/withdraw")
    public ResponseEntity<Transaction> withdraw(
            @Valid @RequestBody WithdrawRequestDto request) {
        Long walletId = request.getWalletId();
        BigDecimal amount = request.getAmount();
        String destination = request.getDestination();
        String destinationType = request.getDestinationType();
        Wallet wallet = walletService.getWalletById(walletId);
        Customer customer = customerService.getCustomerById(wallet.getCustomer().getId());
        if (!authUtil.isEmployee() && !authUtil.isSameUser(customer.getUsername())) {
            throw new AccessDeniedException("Employees can do any transaction, customers can only their own transactions");
        }
        Transaction transaction = transactionService.withdraw(walletId, amount, destination, destinationType);
        return ResponseEntity.ok(transaction);
    }

    @PostMapping("/transactions/approve")
    public ResponseEntity<Transaction> approveTransaction(@Valid @RequestBody ApprovalRequestDto request) {
        Long transactionId = request.getTransactionId();
        Transaction transaction = transactionService.getTransactionById(transactionId);
        Wallet wallet = walletService.getWalletById(transaction.getWallet().getId());
        Customer customer = customerService.getCustomerById(wallet.getCustomer().getId());
        if (!authUtil.isEmployee() && !authUtil.isSameUser(customer.getUsername())) {
            throw new AccessDeniedException("Employees can do any transaction, customers can only their own transactions");
        }
        String status = request.getStatus();
        Transaction transactionApproved = transactionService.approveTransaction(transactionId, status);
        return ResponseEntity.ok(transactionApproved);
    }
}