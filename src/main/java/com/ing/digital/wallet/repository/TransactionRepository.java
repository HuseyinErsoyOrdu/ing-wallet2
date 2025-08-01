package com.ing.digital.wallet.repository;

import com.ing.digital.wallet.model.Transaction;
import com.ing.digital.wallet.model.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByWalletId(Long walletId);
}
