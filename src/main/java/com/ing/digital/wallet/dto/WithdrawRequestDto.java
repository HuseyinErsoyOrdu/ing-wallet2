package com.ing.digital.wallet.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.math.BigDecimal;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WithdrawRequestDto {
    @NotBlank(message = "Wallet Id is mandatory")
    private  Long walletId;
    @NotBlank(message = "Amount is mandatory")
    private BigDecimal amount;
    @NotBlank(message = "Destination is mandatory")
    private String destination;
    @NotBlank(message = "Destination Type is mandatory")
    private String destinationType;
}
