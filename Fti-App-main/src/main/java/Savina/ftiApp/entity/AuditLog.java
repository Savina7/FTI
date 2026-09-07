package Savina.ftiApp.entity;

import Savina.ftiApp.enums.AuditAction;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "AUDIT_LOGS")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "LOG_ID")
    private Long logId;

    @Column(name = "ENTITY_NAME", nullable = false, length = 50)
    private String entityName;

    @Column(name = "ENTITY_ID", nullable = false)
    private Long entityId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CHANGED_BY")
    private User changedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "ACTION", nullable = false, length = 20)
    private AuditAction action;

    @Column(name = "FIELD_NAME", length = 100)
    private String fieldName;

    @Column(name = "OLD_VALUE", length = 4000)
    private String oldValue;

    @Column(name = "NEW_VALUE", length = 4000)
    private String newValue;

    @Column(name = "CHANGED_AT", nullable = false)
    private LocalDateTime changedAt;

    @PrePersist
    public void prePersist() {
        if (this.changedAt == null) {
            this.changedAt = LocalDateTime.now();
        }
    }
}
