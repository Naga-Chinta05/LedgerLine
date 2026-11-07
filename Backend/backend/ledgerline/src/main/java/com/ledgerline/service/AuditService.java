package com.ledgerline.service;

import com.ledgerline.dto.response.AuditLogResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AuditService {
    void logAction(Long actorUserId, String action, String entityType, Long entityId, String details, String ipAddress);
    Page<AuditLogResponse> getAuditLogs(Pageable pageable);
    Page<AuditLogResponse> getAuditLogsForUser(Long userId, Pageable pageable);
}

