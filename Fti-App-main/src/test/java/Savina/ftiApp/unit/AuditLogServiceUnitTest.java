package Savina.ftiApp.unit;

import Savina.ftiApp.entity.AuditLog;
import Savina.ftiApp.entity.User;
import Savina.ftiApp.enums.AuditAction;
import Savina.ftiApp.repository.AuditLogRepository;
import Savina.ftiApp.service.AuditLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceUnitTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditLogService auditLogService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = User.builder()
                .userId(5)
                .email("pedagog@fti.edu.al")
                .emri("Prof")
                .build();
    }

    @Test
    @DisplayName("Unit: logUpdate regjistron ndryshimin me vlerat e vjetra dhe te reja")
    void testLogUpdate() {
        auditLogService.logUpdate("GRADE", 100L, mockUser, "nota", "6", "9");

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository, times(1)).save(captor.capture());

        AuditLog saved = captor.getValue();
        assertThat(saved.getEntityName()).isEqualTo("GRADE");
        assertThat(saved.getEntityId()).isEqualTo(100L);
        assertThat(saved.getAction()).isEqualTo(AuditAction.UPDATE);
        assertThat(saved.getFieldName()).isEqualTo("nota");
        assertThat(saved.getOldValue()).isEqualTo("6");
        assertThat(saved.getNewValue()).isEqualTo("9");
        assertThat(saved.getChangedBy()).isEqualTo(mockUser);
        assertThat(saved.getChangedAt()).isNotNull();
    }

    @Test
    @DisplayName("Unit: logCreate regjistron krijimin e rekordit te ri")
    void testLogCreate() {
        auditLogService.logCreate("STUDENT", 200L, mockUser, "U krijua studenti i ri");

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository, times(1)).save(captor.capture());

        AuditLog saved = captor.getValue();
        assertThat(saved.getEntityName()).isEqualTo("STUDENT");
        assertThat(saved.getAction()).isEqualTo(AuditAction.CREATE);
        assertThat(saved.getNewValue()).isEqualTo("U krijua studenti i ri");
        assertThat(saved.getOldValue()).isNull();
    }

    @Test
    @DisplayName("Unit: getHistoryForEntity kthen listen e log-eve per entitetin e caktuar")
    void testGetHistoryForEntity() {
        AuditLog entry = AuditLog.builder()
                .logId(1L)
                .entityName("GRADE")
                .entityId(100L)
                .action(AuditAction.UPDATE)
                .build();

        when(auditLogRepository.findByEntityNameAndEntityIdOrderByChangedAtDesc("GRADE", 100L))
                .thenReturn(List.of(entry));

        List<AuditLog> result = auditLogService.getHistoryForEntity("grade", 100L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEntityId()).isEqualTo(100L);
        verify(auditLogRepository, times(1)).findByEntityNameAndEntityIdOrderByChangedAtDesc("GRADE", 100L);
    }
}
