package com.ledgerline.exception;

import java.math.BigDecimal;

public class InsufficientFundsException extends RuntimeException {
    public InsufficientFundsException(String accountNumber, BigDecimal currentBalance, BigDecimal requestedAmount) {
        super(String.format("Insufficient funds in account %s. Current balance: %s, Requested: %s",
                accountNumber, currentBalance, requestedAmount));
    }

    public InsufficientFundsException(String message) {
        super(message);
    }
}

