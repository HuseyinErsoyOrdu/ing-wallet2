package com.ing.digital.wallet.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ApprovalRequestDto {
    @NotEmpty(message = "Transaction Id is mandatory")
    private  Long transactionId;
    @NotEmpty(message = "Status is mandatory")
    private String status;
}
