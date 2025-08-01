package com.ing.digital.wallet.controller;

import com.ing.digital.wallet.dto.CreateCustomerRequestDto;
import com.ing.digital.wallet.model.Customer;
import com.ing.digital.wallet.prm.PRM;
import com.ing.digital.wallet.repository.CustomerRepository;
import com.ing.digital.wallet.service.CustomerService;
import com.ing.digital.wallet.util.AuthUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@Tag(name = "Customer API", description = "Operations related to customers")
@SecurityRequirement(name = "Bearer Authentication")
public class CustomerController {

    private final CustomerService customerService;
    private final AuthUtil authUtil;
    private final CustomerRepository customerRepository;

    @Operation(summary = "Get a customer by ID", description = "Employees can get any customer, customers can only get themselves")
    @GetMapping("/{customerId}")
    public ResponseEntity<Customer> getCustomer(
            @Parameter(description = "ID of the customer") @PathVariable Long customerId) {

        Customer customer = customerService.getCustomerById(customerId);

        if (!authUtil.isEmployee() && !authUtil.isSameUser(customer.getUsername())) {
            throw new AccessDeniedException("Employees can get any customer, customers can only get themselves");
        }

        return ResponseEntity.ok(customer);
    }

    @Operation(summary = "Create a new customer", description = "Only employees can create customers")
    @PostMapping("/create")
    public ResponseEntity<Customer> createCustomer(@Valid @RequestBody CreateCustomerRequestDto request) {

        if (!authUtil.isEmployee()) {
            throw new AccessDeniedException("Only employees can create customers");
        }
        Customer customer = new Customer();
        customer.setName(request.getName());
        customer.setSurname(request.getSurname());
        customer.setTckn(request.getTckn());
        customer.setUsername(request.getUsername());
        customer.setPassword(request.getPassword());
        customer.setRole(PRM.Role.CUSTOMER.name());
        customer.setUsername(request.getUsername().toUpperCase());
        if (customerRepository.findByUsername(customer.getUsername()).isPresent())
            throw new RuntimeException("Customer already exists " + customer.getUsername());

        return ResponseEntity.ok(customerService.createCustomer(customer));
    }

}
