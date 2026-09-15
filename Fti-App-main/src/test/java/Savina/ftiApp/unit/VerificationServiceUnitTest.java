package Savina.ftiApp.unit;

import Savina.ftiApp.entity.User;
import Savina.ftiApp.repository.ProfessorPreEnrollmentRepository;
import Savina.ftiApp.repository.StudentPreEnrollmentRepository;
import Savina.ftiApp.repository.UserRepository;
import Savina.ftiApp.service.EmailService;
import Savina.ftiApp.service.VerificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VerificationServiceUnitTest {

    @Mock
    private UserRepository userRepo;

    @Mock
    private EmailService emailService;

    @Mock
    private StudentPreEnrollmentRepository studentPreEnrollmentRepo;

    @Mock
    private ProfessorPreEnrollmentRepository professorPreEnrollmentRepo;

    @InjectMocks
    private VerificationService verificationService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = User.builder()
                .userId(1)
                .email("test@fti.edu.al")
                .emri("Student")
                .mbiemri("Test")
                .verified("N")
                .verificationCode("123456")
                .codeCreatedAt(LocalDateTime.now().minusMinutes(2))
                .build();
    }

    @Test
    @DisplayName("Unit: Verifikimi i kodit kryhet me sukses kur kodi eshte i sakte dhe i vlefshem")
    void testVerifyCodeSuccess() {
        when(userRepo.findById(1)).thenReturn(Optional.of(mockUser));
        when(studentPreEnrollmentRepo.findByEmailIgnoreCase("test@fti.edu.al")).thenReturn(Optional.empty());

        User result = verificationService.verifyCode(1, "123456");

        assertThat(result.getVerified()).isEqualTo("Y");
        assertThat(result.getStatus()).isEqualTo("ACTIVE");
        assertThat(result.getVerificationCode()).isNull();
        assertThat(result.getCodeCreatedAt()).isNull();
        verify(userRepo, times(1)).save(mockUser);
    }

    @Test
    @DisplayName("Unit: Verifikimi deshton kur kodi 6-shifror eshte i pasakte")
    void testVerifyCodeIncorrect() {
        when(userRepo.findById(1)).thenReturn(Optional.of(mockUser));

        assertThatThrownBy(() -> verificationService.verifyCode(1, "999999"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Kodi eshte i pasakte");

        verify(userRepo, never()).save(any());
    }

    @Test
    @DisplayName("Unit: Verifikimi deshton kur kodi ka skaduar (mbi 15 minuta)")
    void testVerifyCodeExpired() {
        mockUser.setCodeCreatedAt(LocalDateTime.now().minusMinutes(20));
        when(userRepo.findById(1)).thenReturn(Optional.of(mockUser));

        assertThatThrownBy(() -> verificationService.verifyCode(1, "123456"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Kodi ka skaduar");

        verify(userRepo, never()).save(any());
    }

    @Test
    @DisplayName("Unit: Verifikimi refuzohet nese llogaria eshte tashme aktive (verified='Y')")
    void testVerifyCodeAlreadyVerified() {
        mockUser.setVerified("Y");
        when(userRepo.findById(1)).thenReturn(Optional.of(mockUser));

        assertThatThrownBy(() -> verificationService.verifyCode(1, "123456"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Llogaria tashme eshte aktive");
    }
}
