package com.ledgerline.service.impl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ledgerline.dto.response.TransactionResponse;
import com.ledgerline.entity.IdempotencyRecord;
import com.ledgerline.exception.IdempotencyConflictException;
import com.ledgerline.repository.IdempotencyRecordRepository;
import com.ledgerline.service.IdempotencyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Optional;
@Service
public class IdempotencyServiceImpl implements IdempotencyService {
    private static final Logger log = LoggerFactory.getLogger(IdempotencyServiceImpl.class);
    private final IdempotencyRecordRepository idempotencyRecordRepository;
    private final ObjectMapper objectMapper;
    public IdempotencyServiceImpl(IdempotencyRecordRepository idempotencyRecordRepository) {
        this.idempotencyRecordRepository = idempotencyRecordRepository;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }
    @Override
    public String computeHash(Object... parameters) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String combined = Arrays.toString(parameters);
            byte[] encodedhash = digest.digest(combined.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : encodedhash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
    @Override
    @Transactional(readOnly = true)
    public Optional<TransactionResponse> getCompletedResponse(Long userId, String idempotencyKey, String currentHash) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return Optional.empty();
        }
        Optional<IdempotencyRecord> existingOpt = idempotencyRecordRepository.findByUserIdAndIdempotencyKey(userId, idempotencyKey);
        if (existingOpt.isEmpty()) {
            return Optional.empty();
        }
        IdempotencyRecord record = existingOpt.get();
        if (!record.getRequestHash().equals(currentHash)) {
            throw new IdempotencyConflictException(
                    String.format("Idempotency key '%s' has already been used with different request parameters.", idempotencyKey));
        }
        if ("PROCESSING".equals(record.getStatus())) {
            throw new IdempotencyConflictException(
                    String.format("A request with idempotency key '%s' is currently processing. Please wait.", idempotencyKey));
        }
        if ("COMPLETED".equals(record.getStatus()) && record.getResponseBody() != null) {
            try {
                TransactionResponse cached = objectMapper.readValue(record.getResponseBody(), TransactionResponse.class);
                return Optional.of(cached);
            } catch (Exception ex) {
                log.error("Failed to deserialize idempotency cached response", ex);
            }
        }
        return Optional.empty();
    }
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public IdempotencyRecord createProcessingRecord(Long userId, String idempotencyKey, String requestHash) {
        IdempotencyRecord record = new IdempotencyRecord();
        record.setUserId(userId);
        record.setIdempotencyKey(idempotencyKey);
        record.setRequestHash(requestHash);
        record.setStatus("PROCESSING");
        return idempotencyRecordRepository.save(record);
    }
@Override
 @Transactional
 public void completeRecord(IdempotencyRecord record, TransactionResponse response) {
     record.setStatus("COMPLETED");
     record.setTransactionId(response.getId());
     try {
         record.setResponseBody(objectMapper.writeValueAsString(response));
     } catch (Exception ex) {
         log.error("Failed to serialize transaction response for idempotency", ex);
     }
     idempotencyRecordRepository.save(record);
 }
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void failRecord(IdempotencyRecord record) {
        record.setStatus("FAILED");
        idempotencyRecordRepository.save(record);
    }
}

