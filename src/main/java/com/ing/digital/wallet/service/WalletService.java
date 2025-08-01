package com.ing.digital.wallet.service;

import com.ing.digital.wallet.model.Customer;
import com.ing.digital.wallet.model.Transaction;
import com.ing.digital.wallet.model.Wallet;
import com.ing.digital.wallet.prm.PRM;
import com.ing.digital.wallet.repository.CustomerRepository;
import com.ing.digital.wallet.repository.TransactionRepository;
import com.ing.digital.wallet.repository.WalletRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WalletService {
    private final WalletRepository walletRepository;
    private final CustomerRepository customerRepository;
    private final TransactionRepository transactionRepository;

    public WalletService(WalletRepository walletRepository, CustomerRepository customerRepository, TransactionRepository transactionRepository) {
        this.walletRepository = walletRepository;
        this.customerRepository = customerRepository;
        this.transactionRepository = transactionRepository;
    }

    public Wallet createWallet(Long customerId, String name, String currency, boolean shop, boolean withdraw) {
        try {
            PRM.Currency.valueOf(currency);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unsupported currency: " + currency);
        }
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        Wallet wallet = new Wallet();
        wallet.setCustomer(customer);
        wallet.setWalletName(name);
        wallet.setCurrency(currency);
        wallet.setActiveForShopping(shop);
        wallet.setActiveForWithdraw(withdraw);
        return walletRepository.save(wallet);
    }

    public Wallet getWalletById(Long id) {
        return walletRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));
    }

    public List<Wallet> getWallets(Long customerId) {
        return walletRepository.findByCustomerId(customerId);
    }

    public List<Wallet> getWalletsByCustomerAndCurrency(Long customerId, String currency) {
        try {
            PRM.Currency.valueOf(currency);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unsupported currency: " + currency);
        }
        return walletRepository.findByCustomerIdAndCurrency(customerId, currency);
    }

    public List<Transaction> listTransactions(Long walletId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));
        return transactionRepository.findByWalletId(wallet.getId());
    }
}
