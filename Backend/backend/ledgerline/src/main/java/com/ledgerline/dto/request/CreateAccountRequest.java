package com.ledgerline.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

public class CreateAccountRequest {
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a 3-letter ISO code, e.g. INR, USD")
    private String currency = "INR";

    @DecimalMin(value = "0.00", message = "Initial deposit cannot be negative")
    private BigDecimal initialDeposit = BigDecimal.ZERO;

    public CreateAccountRequest() {}

    public CreateAccountRequest(String currency, BigDecimal initialDeposit) {
        this.currency = currency != null ? currency : "INR";
        this.initialDeposit = initialDeposit != null ? initialDeposit : BigDecimal.ZERO;
    }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public BigDecimal getInitialDeposit() { return initialDeposit; }
    public void setInitialDeposit(BigDecimal initialDeposit) { this.initialDeposit = initialDeposit; }
}

