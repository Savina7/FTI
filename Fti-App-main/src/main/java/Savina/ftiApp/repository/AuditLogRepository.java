package Savina.ftiApp.repository;

import Savina.ftiApp.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    // Historiku i ndryshimeve per nje rekord specifik (p.sh. per Noten me ID 15)
    List<AuditLog> findByEntityNameAndEntityIdOrderByChangedAtDesc(String entityName, Long entityId);

    // Historiku per te gjithe entitetin (p.sh. te gjitha ndryshimet e notave)
    List<AuditLog> findByEntityNameOrderByChangedAtDesc(String entityName);

    // Historiku i veprimeve te bera nga nje perdorues specifik
    List<AuditLog> findByChangedBy_UserIdOrderByChangedAtDesc(Integer userId);
}
