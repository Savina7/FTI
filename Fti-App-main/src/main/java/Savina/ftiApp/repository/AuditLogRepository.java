package Savina.ftiApp.repository;

import Savina.ftiApp.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByEntityNameAndEntityIdOrderByChangedAtDesc(String entityName, Long entityId);

    List<AuditLog> findByEntityNameOrderByChangedAtDesc(String entityName);

    List<AuditLog> findByChangedBy_UserIdOrderByChangedAtDesc(Integer userId);
}
