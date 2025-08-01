package com.ing.digital.wallet.prm;

public class PRM {

    public enum Currency {
        TRY, USD, EUR
    }

    public enum TransactionType {
        DEPOSIT, WITHDRAW
    }

    public enum TransactionStatus {
        PENDING, APPROVED, DENIED
    }

    public enum OppositePartyType {
        IBAN, PAYMENT
    }

    public enum Role {
        CUSTOMER, EMPLOYEE
    }
}
