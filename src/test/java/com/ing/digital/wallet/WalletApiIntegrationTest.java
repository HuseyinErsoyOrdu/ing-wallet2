package com.ing.digital.wallet;

import com.ing.digital.wallet.model.Customer;
import com.ing.digital.wallet.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class WalletApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    public void testGetWallets_Integration() throws Exception {
        Customer customer = new Customer();
        customer.setName("Ali");
        customer.setSurname("Atak");
        customer.setTckn("12345678901");
        customer.setRole("CUSTOMER");
        Customer c = customerRepository.save(customer);
        System.out.println(c.getTckn());

    }
}

