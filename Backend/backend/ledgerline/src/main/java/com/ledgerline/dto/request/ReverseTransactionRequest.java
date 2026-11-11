package com.ledgerline.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ReverseTransactionRequest {

    @NotBlank(message = "Reversal reason is required for audit compliance")
    @Size(max = 250, message = "Reason cannot exceed 250 characters")
    private String reason;

    public ReverseTransactionRequest() {
    }

    public ReverseTransactionRequest(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
