package com.ledgerline.service;

import com.ledgerline.dto.response.TransactionResponse;
import com.ledgerline.entity.IdempotencyRecord;

import java.util.Optional;

public interface IdempotencyService {
    String computeHash(Object... parameters);
    Optional<TransactionResponse> getCompletedResponse(Long userId, String idempotencyKey, String currentHash);
    IdempotencyRecord createProcessingRecord(Long userId, String idempotencyKey, String requestHash);
    void completeRecord(IdempotencyRecord record, TransactionResponse response);
    void failRecord(IdempotencyRecord record);
}

