package com.ing.digital.wallet.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateWalletRequestDto {
    @NotBlank(message = "Customer Id is mandatory")
    private Long customerId;
    @NotBlank(message = "Name is mandatory")
    private String name;
    @NotBlank(message = "Currency is mandatory")
    private String currency;

    private boolean shop;
    private boolean withdraw;
}
