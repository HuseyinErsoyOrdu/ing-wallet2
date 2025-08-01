package com.ing.digital.wallet.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ing.digital.wallet.dto.CreateCustomerRequestDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testCreateEmployeeWithoutAuth() throws Exception {
        CreateCustomerRequestDto requestDto = new CreateCustomerRequestDto();
        requestDto.setName("Turkan");
        requestDto.setSurname("Atak");
        requestDto.setTckn("12345678901");
        requestDto.setUsername("turkan");
        requestDto.setPassword("123456");

        mockMvc.perform(post("/api/auth/employee/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .with(csrf())) // Only needed if CSRF is enabled
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Turkan"));
    }
}


