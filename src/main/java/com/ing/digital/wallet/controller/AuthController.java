package com.ing.digital.wallet.controller;

import com.ing.digital.wallet.dto.CreateCustomerRequestDto;
import com.ing.digital.wallet.dto.LoginRequestDto;
import com.ing.digital.wallet.model.Customer;
import com.ing.digital.wallet.prm.PRM;
import com.ing.digital.wallet.repository.CustomerRepository;
import com.ing.digital.wallet.service.CustomerService;
import com.ing.digital.wallet.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authorization API", description = "Operations related to authorization")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil; // Custom class for generating JWT
    private final CustomerService customerService;
    private final CustomerRepository customerRepository;

    @Operation(summary = "Create a new employee", description = "Only employees can create customers")
    @PostMapping("/employee/create")
    public ResponseEntity<Customer> createEmployee(@Valid @RequestBody CreateCustomerRequestDto request) {
        Customer customer = new Customer();
        customer.setName(request.getName());
        customer.setSurname(request.getSurname());
        customer.setTckn(request.getTckn());
        customer.setPassword(request.getPassword());
        customer.setRole(PRM.Role.EMPLOYEE.name());
        customer.setUsername(request.getUsername().toUpperCase());
        if (customerRepository.findByUsername(customer.getUsername()).isPresent())
            throw new RuntimeException("User already exists " + customer.getUsername());

        Customer customerOut = customerService.createCustomer(customer);
        ResponseEntity<Customer> x = ResponseEntity.ok(customerOut);
        return x;
    }

    @Operation(summary = "User Login", description = "The user can be employee or customer")
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDto request) {
        Customer user = customerRepository.findByUsername(request.getUsername().toUpperCase())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername().toUpperCase(),
                        request.getPassword(),
                        Collections.singleton(new SimpleGrantedAuthority(user.getRole()))));

        String jwtToken = jwtUtil.generateToken(auth);

        return ResponseEntity.ok(Map.of("token", jwtToken));
    }

    @Operation(summary = "Authorized User", description = "Authenticated user detail is returned ")
    @GetMapping("/me")
    public String getLoggedInUser(Authentication authentication) {
        return "Logged in as: " + authentication.getName();
    }
}

