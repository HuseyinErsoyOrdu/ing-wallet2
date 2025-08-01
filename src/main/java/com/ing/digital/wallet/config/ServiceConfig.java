package com.ing.digital.wallet.config;

import com.ing.digital.wallet.repository.CustomerRepository;
import com.ing.digital.wallet.repository.TransactionRepository;
import com.ing.digital.wallet.repository.WalletRepository;
import com.ing.digital.wallet.service.TransactionService;
import com.ing.digital.wallet.service.WalletService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
public class ServiceConfig {
    @Bean
    public TransactionService transactionService(WalletRepository walletRepository, TransactionRepository transactionRepository, WalletProperties walletProperties) {
        return new TransactionService(walletRepository, transactionRepository, walletProperties);
    }
    @Bean
    public WalletService walletService(WalletRepository walletRepository, CustomerRepository customerRepository, TransactionRepository transactionRepository) {
        return new WalletService(walletRepository, customerRepository, transactionRepository);
    }

}
