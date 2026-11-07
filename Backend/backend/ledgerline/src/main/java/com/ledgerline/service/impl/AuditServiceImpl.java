package com.ledgerline.service.impl;

import com.ledgerline.dto.response.AuditLogResponse;
import com.ledgerline.entity.AuditLog;
import com.ledgerline.entity.User;
import com.ledgerline.repository.AuditLogRepository;
import com.ledgerline.repository.UserRepository;
import com.ledgerline.service.AuditService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    public AuditServiceImpl(AuditLogRepository auditLogRepository, UserRepository userRepository) {
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAction(Long actorUserId, String action, String entityType, Long entityId, String details, String ipAddress) {
        AuditLog log = new AuditLog();
        log.setActorUserId(actorUserId);
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setDetails(details);
        log.setIpAddress(ipAddress != null ? ipAddress : "0.0.0.0");
        auditLogRepository.save(log);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogResponse> getAuditLogs(Pageable pageable) {
        return auditLogRepository.findAllByOrderByTimestampDesc(pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogResponse> getAuditLogsForUser(Long userId, Pageable pageable) {
        return auditLogRepository.findByActorUserIdOrderByTimestampDesc(userId, pageable)
                .map(this::mapToResponse);
    }

    private AuditLogResponse mapToResponse(AuditLog log) {
        String actorEmail = null;
        if (log.getActorUserId() != null) {
            actorEmail = userRepository.findById(log.getActorUserId())
                    .map(User::getEmail)
                    .orElse(null);
        }
        return new AuditLogResponse(
                log.getId(),
                log.getActorUserId(),
                actorEmail,
                log.getAction(),
                log.getEntityType(),
                log.getEntityId(),
                log.getDetails(),
                log.getIpAddress(),
                log.getTimestamp()
        );
    }
}

