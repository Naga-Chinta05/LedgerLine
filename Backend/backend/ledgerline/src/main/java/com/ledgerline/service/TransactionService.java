package com.ledgerline.service;

import com.ledgerline.dto.request.DepositRequest;
import com.ledgerline.dto.request.TransferRequest;
import com.ledgerline.dto.request.WithdrawalRequest;
import com.ledgerline.dto.response.TransactionResponse;
import com.ledgerline.security.UserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TransactionService {
    TransactionResponse deposit(DepositRequest request, String idempotencyKey, UserPrincipal principal, String ipAddress);
    TransactionResponse withdraw(WithdrawalRequest request, String idempotencyKey, UserPrincipal principal, String ipAddress);
    TransactionResponse transfer(TransferRequest request, String idempotencyKey, UserPrincipal principal, String ipAddress);
    TransactionResponse getTransactionById(Long transactionId, UserPrincipal principal);
    Page<TransactionResponse> getTransactionsForUser(UserPrincipal principal, Pageable pageable);
    Page<TransactionResponse> getTransactionsForAccount(Long accountId, UserPrincipal principal, Pageable pageable);
    Page<TransactionResponse> getAllTransactions(Pageable pageable);
}

