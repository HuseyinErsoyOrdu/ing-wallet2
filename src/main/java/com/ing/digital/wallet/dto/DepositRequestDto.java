package com.ing.digital.wallet.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.math.BigDecimal;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DepositRequestDto {
    @NotBlank(message = "Wallet Id is mandatory")
    private Long walletId;
    @NotBlank(message = "Amount is mandatory")
    private BigDecimal amount;
    @NotBlank(message = "Source Type is mandatory")
    private String sourceType;
    @NotBlank(message = "Source is mandatory")
    private String source;
}
