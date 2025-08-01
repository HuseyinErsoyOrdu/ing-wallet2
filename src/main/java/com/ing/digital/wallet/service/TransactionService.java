package com.ing.digital.wallet.service;

import com.ing.digital.wallet.config.WalletProperties;
import com.ing.digital.wallet.model.Transaction;
import com.ing.digital.wallet.model.Wallet;
import com.ing.digital.wallet.prm.PRM;
import com.ing.digital.wallet.repository.TransactionRepository;
import com.ing.digital.wallet.repository.WalletRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class TransactionService {
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final WalletProperties walletProperties;
    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);

    public TransactionService(WalletRepository walletRepository, TransactionRepository transactionRepository, WalletProperties walletProperties) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
        this.walletProperties = walletProperties;
    }

    public Transaction getTransactionById(Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));
    }

    public Transaction deposit(Long walletId, BigDecimal amount, String sourceType, String source) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        PRM.OppositePartyType partyType;
        try {
            partyType = PRM.OppositePartyType.valueOf(sourceType);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid source type: must be IBAN or PAYMENT");
        }

        Transaction transaction = new Transaction();
        transaction.setWallet(wallet);
        transaction.setAmount(amount);
        transaction.setType(PRM.TransactionType.DEPOSIT.name());
        transaction.setOppositePartyType(partyType.name());
        transaction.setOppositeParty(source);

        if (amount.compareTo(walletProperties.getAutoApproveThreshold()) > 0) {
            transaction.setStatus(PRM.TransactionStatus.PENDING.name());
            wallet.setBalance(wallet.getBalance().add(amount));
        } else {
            transaction.setStatus(PRM.TransactionStatus.APPROVED.name());
            wallet.setBalance(wallet.getBalance().add(amount));
            wallet.setUsableBalance(wallet.getUsableBalance().add(amount));
        }

        walletRepository.save(wallet);
        return transactionRepository.save(transaction);
    }

    public Transaction withdraw(Long walletId, BigDecimal amount, String destination, String destinationType) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        // Validate destination type (IBAN or PAYMENT)
        PRM.OppositePartyType partyType;
        try {
            partyType = PRM.OppositePartyType.valueOf(destinationType);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid destination type: must be IBAN or PAYMENT");
        }

        // Check if wallet is active for withdraw/shopping
        if (partyType == PRM.OppositePartyType.IBAN) {
            if (!wallet.isActiveForWithdraw()) {
                throw new IllegalStateException("Withdrawals are not allowed on this wallet.");
            }
        }else{
            if (!wallet.isActiveForShopping()) {
                throw new IllegalStateException("Shopping is not allowed on this wallet.");
            }
        }

        // Check if usable balance is sufficient for withdrawal
        if (wallet.getUsableBalance().compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient usable balance.");
        }

        Transaction transaction = new Transaction();
        transaction.setWallet(wallet);
        transaction.setAmount(amount);
        transaction.setType(PRM.TransactionType.WITHDRAW.name());
        transaction.setOppositeParty(destination);
        transaction.setOppositePartyType(partyType.name());

        if (amount.compareTo(walletProperties.getAutoApproveThreshold()) > 0) {
            // Pending withdraw: only reduce usable balance
            transaction.setStatus(PRM.TransactionStatus.PENDING.name());
            wallet.setUsableBalance(wallet.getUsableBalance().subtract(amount));
        } else {
            // Approved withdraw: reduce both balance and usable balance
            transaction.setStatus(PRM.TransactionStatus.APPROVED.name());
            wallet.setBalance(wallet.getBalance().subtract(amount));
            wallet.setUsableBalance(wallet.getUsableBalance().subtract(amount));
        }

        walletRepository.save(wallet);
        return transactionRepository.save(transaction);
    }

    public Transaction approveTransaction(Long transactionId, String statusStr) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        PRM.TransactionStatus newStatus;
        try {
            newStatus = PRM.TransactionStatus.valueOf(statusStr);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status: must be APPROVED or DENIED");
        }

        if (newStatus == PRM.TransactionStatus.APPROVED && transaction.getStatus().equals(PRM.TransactionStatus.APPROVED.name())) {
            throw new IllegalStateException("Transaction is already approved");
        }
        if (newStatus == PRM.TransactionStatus.DENIED && transaction.getStatus().equals(PRM.TransactionStatus.DENIED.name())) {
            throw new IllegalStateException("Transaction is already denied");
        }
        if (newStatus == PRM.TransactionStatus.PENDING) {
            throw new IllegalArgumentException("Cannot change status back to PENDING");
        }

        if (newStatus == PRM.TransactionStatus.APPROVED && transaction.getStatus().equals(PRM.TransactionStatus.DENIED.name())) {
            throw new IllegalStateException("The Transaction denied cannot be approved");
        }
        if (newStatus == PRM.TransactionStatus.DENIED && transaction.getStatus().equals(PRM.TransactionStatus.APPROVED.name())) {
            throw new IllegalStateException("The transaction approved cannot be denied");
        }

        Wallet wallet = transaction.getWallet();
        BigDecimal amount = transaction.getAmount();
        PRM.TransactionType type = PRM.TransactionType.valueOf(transaction.getType());
        PRM.TransactionStatus oldStatus = PRM.TransactionStatus.valueOf(transaction.getStatus());

        if (oldStatus == newStatus) {
            log.warn("No status change: transaction {} is already {}", transactionId, newStatus);
            return transaction;
        }

        log.info("Reverting previous impact of transaction {} from status {}", transactionId, oldStatus);

        // If already approved or denied, revert previous balance impact before applying new status
        if (oldStatus == PRM.TransactionStatus.APPROVED) {
            if (type == PRM.TransactionType.DEPOSIT) {
                wallet.setBalance(wallet.getBalance().subtract(amount));
                wallet.setUsableBalance(wallet.getUsableBalance().subtract(amount));
            } else if (type == PRM.TransactionType.WITHDRAW) {
                wallet.setBalance(wallet.getBalance().add(amount));
                wallet.setUsableBalance(wallet.getUsableBalance().add(amount));
            }
        } else if (oldStatus == PRM.TransactionStatus.PENDING) {
            if (type == PRM.TransactionType.DEPOSIT) {
                wallet.setBalance(wallet.getBalance().subtract(amount));
            } else if (type == PRM.TransactionType.WITHDRAW) {
                wallet.setUsableBalance(wallet.getUsableBalance().add(amount));
            }
        }

        log.info("Applying new impact of transaction {} with status {}", transactionId, newStatus);

        // Apply the new status effects
        if (newStatus == PRM.TransactionStatus.APPROVED) {
            if (type == PRM.TransactionType.DEPOSIT) {
                wallet.setBalance(wallet.getBalance().add(amount));
                wallet.setUsableBalance(wallet.getUsableBalance().add(amount));
            } else if (type == PRM.TransactionType.WITHDRAW) {
                wallet.setBalance(wallet.getBalance().subtract(amount));
                wallet.setUsableBalance(wallet.getUsableBalance().subtract(amount));
            }
        }
        // if DENIED, no changes needed beyond reverting the old pending effects

        transaction.setStatus(newStatus.name());
        walletRepository.save(wallet);

        log.info("Transaction {} updated to status {}", transactionId, newStatus);

        return transactionRepository.save(transaction);
    }
}
