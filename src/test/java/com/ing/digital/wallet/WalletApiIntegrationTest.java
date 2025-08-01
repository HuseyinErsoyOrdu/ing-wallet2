package com.ing.digital.wallet;

import com.ing.digital.wallet.dto.*;
import com.ing.digital.wallet.model.Customer;
import com.ing.digital.wallet.model.Transaction;
import com.ing.digital.wallet.model.Wallet;
import com.ing.digital.wallet.prm.PRM;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class WalletApiIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void Test(){
        createEmployeeTest();
        String token = loginTest();
        createCustomerTest(token);
        createWalltTest(token);
        listCustomerWalletsTest(token);
        depositTest(token);
        approvaTest(token);
        withdrawTest(token);
        approvaTest2(token);
        listWalletTransactionsTest(token);
    }

    public void createEmployeeTest() {
        CreateCustomerRequestDto request = new CreateCustomerRequestDto();
        request.setName("Ali");
        request.setSurname("Atak");
        request.setTckn("12345678901");
        request.setUsername("ALI");
        request.setPassword("123456");

        // Set headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", ""); // OR remove this line if you want no header at all

        HttpEntity<CreateCustomerRequestDto> httpEntity = new HttpEntity<>(request, headers);

        // Use exchange method to send request with headers
        ResponseEntity<Customer> response = restTemplate.exchange(
                "/api/auth/employee/create",
                HttpMethod.POST,
                httpEntity,
                Customer.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(request.getUsername().toUpperCase(), response.getBody().getUsername());
    }

    public String loginTest() {
        LoginRequestDto request = new LoginRequestDto();
        request.setUsername("ALI");
        request.setPassword("123456");

        // Set headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", ""); // OR remove this line if you want no header at all

        HttpEntity<LoginRequestDto> httpEntity = new HttpEntity<>(request, headers);

        // Use exchange method to send request with headers
        ResponseEntity<TokenDto> response = restTemplate.exchange(
                "/api/auth/login",
                HttpMethod.POST,
                httpEntity,
                TokenDto.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());

        System.out.println(response.getBody());

        return response.getBody().getToken();
    }

    public void createCustomerTest(String token) {
        CreateCustomerRequestDto request = new CreateCustomerRequestDto();
        request.setName("Berkay");
        request.setSurname("Ersoy");
        request.setTckn("92865432101");
        request.setUsername("berkay");
        request.setPassword("123456");

        // Set headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token); // OR remove this line if you want no header at all

        HttpEntity<CreateCustomerRequestDto> httpEntity = new HttpEntity<>(request, headers);

        // Use exchange method to send request with headers
        ResponseEntity<Customer> response = restTemplate.exchange(
                "/api/customers/create",
                HttpMethod.POST,
                httpEntity,
                Customer.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(request.getUsername().toUpperCase(), response.getBody().getUsername());
    }

    public void createWalltTest(String token) {
        CreateWalletRequestDto request = new CreateWalletRequestDto();
        request.setName("Wallt 1");
        request.setCurrency(PRM.Currency.EUR.name());
        request.setCustomerId(2L);
        request.setShop(true);
        request.setWithdraw(true);

        // Set headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token); // OR remove this line if you want no header at all

        HttpEntity<CreateWalletRequestDto> httpEntity = new HttpEntity<>(request, headers);

        // Use exchange method to send request with headers
        ResponseEntity<Wallet> response = restTemplate.exchange(
                "/api/wallets/create",
                HttpMethod.POST,
                httpEntity,
                Wallet.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(request.getCurrency(), response.getBody().getCurrency());
    }

    public void listCustomerWalletsTest(String token) {
        // Set headers
        HttpHeaders headers = new HttpHeaders();
        //headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token); // OR remove this line if you want no header at all

        HttpEntity<Void> httpEntity = new HttpEntity<>( headers);

        // Use exchange method to send request with headers
        ResponseEntity<List<Wallet>> response = restTemplate.exchange(
                "/api/wallets/2",
                HttpMethod.GET,
                httpEntity,
                new ParameterizedTypeReference<List<Wallet>>() {}
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertNotNull(response.getBody()); // Fixed line
        Assertions.assertFalse(response.getBody().isEmpty(), "Wallet list should not be empty"); // Optional

        System.out.println(response.getBody());
    }

    public void depositTest(String token) {
        DepositRequestDto request = new DepositRequestDto();
        request.setWalletId(1L);
        request.setAmount(new BigDecimal(1001));
        request.setSource("P-123445");
        request.setSourceType(PRM.OppositePartyType.PAYMENT.name());

        // Set headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token); // OR remove this line if you want no header at all

        HttpEntity<DepositRequestDto> httpEntity = new HttpEntity<>(request, headers);

        // Use exchange method to send request with headers
        ResponseEntity<Transaction> response = restTemplate.exchange(
                "/api/transactions/deposit",
                HttpMethod.POST,
                httpEntity,
                Transaction.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(PRM.TransactionStatus.PENDING.name(), Objects.requireNonNull(response.getBody()).getStatus());
        assertEquals(request.getAmount().setScale(2, RoundingMode.HALF_UP), response.getBody().getWallet().getBalance().setScale(2, RoundingMode.HALF_UP));
        assertEquals(new BigDecimal(0).setScale(2, RoundingMode.HALF_UP), response.getBody().getWallet().getUsableBalance().setScale(2, RoundingMode.HALF_UP));
    }

    public void approvaTest(String token) {
        ApprovalRequestDto request = new ApprovalRequestDto();
        request.setTransactionId(1L);
        request.setStatus(PRM.TransactionStatus.APPROVED.name());

        // Set headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token); // OR remove this line if you want no header at all

        HttpEntity<ApprovalRequestDto> httpEntity = new HttpEntity<>(request, headers);

        // Use exchange method to send request with headers
        ResponseEntity<Transaction> response = restTemplate.exchange(
                "/api/transactions/approve",
                HttpMethod.POST,
                httpEntity,
                Transaction.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(PRM.TransactionStatus.APPROVED.name(), Objects.requireNonNull(response.getBody()).getStatus());
        assertEquals(response.getBody().getWallet().getUsableBalance().setScale(2, RoundingMode.HALF_UP), response.getBody().getWallet().getBalance().setScale(2, RoundingMode.HALF_UP));
    }

    public void withdrawTest(String token) {
        WithdrawRequestDto request = new WithdrawRequestDto();
        request.setWalletId(1L);
        request.setAmount(new BigDecimal(1001));
        request.setDestination("P-123445");
        request.setDestinationType(PRM.OppositePartyType.PAYMENT.name());

        // Set headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token); // OR remove this line if you want no header at all

        HttpEntity<WithdrawRequestDto> httpEntity = new HttpEntity<>(request, headers);

        // Use exchange method to send request with headers
        ResponseEntity<Transaction> response = restTemplate.exchange(
                "/api/transactions/withdraw",
                HttpMethod.POST,
                httpEntity,
                Transaction.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(PRM.TransactionStatus.PENDING.name(), Objects.requireNonNull(response.getBody()).getStatus());
        assertEquals(response.getBody().getWallet().getBalance().setScale(2, RoundingMode.HALF_UP), response.getBody().getWallet().getUsableBalance().add(request.getAmount()).setScale(2, RoundingMode.HALF_UP));
    }

    public void approvaTest2(String token) {
        ApprovalRequestDto request = new ApprovalRequestDto();
        request.setTransactionId(2L);
        request.setStatus(PRM.TransactionStatus.APPROVED.name());

        // Set headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token); // OR remove this line if you want no header at all

        HttpEntity<ApprovalRequestDto> httpEntity = new HttpEntity<>(request, headers);

        // Use exchange method to send request with headers
        ResponseEntity<Transaction> response = restTemplate.exchange(
                "/api/transactions/approve",
                HttpMethod.POST,
                httpEntity,
                Transaction.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(PRM.TransactionStatus.APPROVED.name(), Objects.requireNonNull(response.getBody()).getStatus());
        assertEquals(response.getBody().getWallet().getUsableBalance().setScale(2, RoundingMode.HALF_UP), response.getBody().getWallet().getBalance().setScale(2, RoundingMode.HALF_UP));
    }

    public void listWalletTransactionsTest(String token) {
        // Set headers
        HttpHeaders headers = new HttpHeaders();
        //headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token); // OR remove this line if you want no header at all

        HttpEntity<Void> httpEntity = new HttpEntity<>( headers);

        // Use exchange method to send request with headers
        ResponseEntity<List<Transaction>> response = restTemplate.exchange(
                "/api/wallets/1/transactions",
                HttpMethod.GET,
                httpEntity,
                new ParameterizedTypeReference<List<Transaction>>() {}
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertNotNull(response.getBody()); // Fixed line
        Assertions.assertFalse(response.getBody().isEmpty(), "Transactionlist should not be empty"); // Optional

        System.out.println(response.getBody());
    }
}

