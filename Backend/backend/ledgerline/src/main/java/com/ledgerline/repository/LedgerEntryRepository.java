package com.ledgerline.repository;

import com.ledgerline.entity.LedgerEntry;
import com.ledgerline.enums.EntryType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Repository
public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, Long> {

    List<LedgerEntry> findByTransactionIdOrderByIdAsc(Long transactionId);

    Page<LedgerEntry> findByAccountIdOrderByCreatedAtDesc(Long accountId, Pageable pageable);

    @Query("SELECT COALESCE(SUM(l.amount), 0) FROM LedgerEntry l WHERE l.entryType = :entryType")
    BigDecimal sumAmountByEntryType(@Param("entryType") EntryType entryType);

    @Query("SELECT COALESCE(SUM(l.amount), 0) FROM LedgerEntry l WHERE l.account.id = :accountId AND l.entryType = :entryType")
    BigDecimal sumAmountByAccountAndEntryType(@Param("accountId") Long accountId, @Param("entryType") EntryType entryType);

    @Query("SELECT l FROM LedgerEntry l WHERE l.account.id = :accountId AND l.createdAt >= :startDate AND l.createdAt <= :endDate ORDER BY l.createdAt ASC, l.id ASC")
    List<LedgerEntry> findByAccountIdAndDateRange(@Param("accountId") Long accountId,
                                                  @Param("startDate") Instant startDate,
                                                  @Param("endDate") Instant endDate);

    @Query("SELECT COALESCE(SUM(l.amount), 0) FROM LedgerEntry l WHERE l.account.id = :accountId AND l.entryType = :entryType AND l.createdAt < :beforeDate")
    BigDecimal sumAmountByAccountAndEntryTypeBefore(@Param("accountId") Long accountId,
                                                    @Param("entryType") EntryType entryType,
                                                    @Param("beforeDate") Instant beforeDate);
}
