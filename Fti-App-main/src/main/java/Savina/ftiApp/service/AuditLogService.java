package Savina.ftiApp.service;

import Savina.ftiApp.entity.AuditLog;
import Savina.ftiApp.entity.User;
import Savina.ftiApp.enums.AuditAction;
import Savina.ftiApp.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Transactional
    public void log(String entityName, Long entityId, User user, AuditAction action,
                    String fieldName, Object oldValue, Object newValue) {
        try {
            AuditLog logEntry = AuditLog.builder()
                    .entityName(entityName != null ? entityName.toUpperCase() : "UNKNOWN")
                    .entityId(entityId)
                    .changedBy(user)
                    .action(action)
                    .fieldName(fieldName)
                    .oldValue(oldValue != null ? String.valueOf(oldValue) : null)
                    .newValue(newValue != null ? String.valueOf(newValue) : null)
                    .changedAt(LocalDateTime.now())
                    .build();

            auditLogRepository.save(logEntry);
        } catch (Exception e) {
            log.error("Gabim gjate ruajtjes se audit log per {}-{}: {}", entityName, entityId, e.getMessage(), e);
        }
    }

    @Transactional
    public void logUpdate(String entityName, Long entityId, User user,
                          String fieldName, Object oldValue, Object newValue) {
        log(entityName, entityId, user, AuditAction.UPDATE, fieldName, oldValue, newValue);
    }

    @Transactional
    public void logCreate(String entityName, Long entityId, User user, String description) {
        log(entityName, entityId, user, AuditAction.CREATE, "ALL", null, description);
    }

    @Transactional
    public void logDelete(String entityName, Long entityId, User user, String description) {
        log(entityName, entityId, user, AuditAction.DELETE, "ALL", description, null);
    }

    @Transactional(readOnly = true)
    public List<AuditLog> getHistoryForEntity(String entityName, Long entityId) {
        return auditLogRepository.findByEntityNameAndEntityIdOrderByChangedAtDesc(
                entityName != null ? entityName.toUpperCase() : "", entityId);
    }
}
