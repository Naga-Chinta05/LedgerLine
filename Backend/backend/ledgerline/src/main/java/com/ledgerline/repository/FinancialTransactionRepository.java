package com.ledgerline.repository;

import com.ledgerline.entity.FinancialTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FinancialTransactionRepository extends JpaRepository<FinancialTransaction, Long> {

    Page<FinancialTransaction> findByCreatedByOrderByCreatedAtDesc(Long createdBy, Pageable pageable);

    @Query("SELECT t FROM FinancialTransaction t WHERE t.sourceAccount.id = :accountId OR t.destinationAccount.id = :accountId ORDER BY t.createdAt DESC")
    Page<FinancialTransaction> findByAccountId(@Param("accountId") Long accountId, Pageable pageable);

    @Query("SELECT t FROM FinancialTransaction t WHERE t.sourceAccount.id IN :accountIds OR t.destinationAccount.id IN :accountIds OR t.createdBy = :userId ORDER BY t.createdAt DESC")
    Page<FinancialTransaction> findByUserInvolvement(@Param("userId") Long userId, @Param("accountIds") List<Long> accountIds, Pageable pageable);

    List<FinancialTransaction> findTop10ByCreatedByOrderByCreatedAtDesc(Long createdBy);
}

