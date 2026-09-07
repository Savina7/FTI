package Savina.ftiApp.unit;

import Savina.ftiApp.dto.requestDTO.ForgotPasswordRequest;
import Savina.ftiApp.dto.requestDTO.ResetPasswordRequest;
import Savina.ftiApp.dto.requestDTO.VerifyResetCodeRequest;
import Savina.ftiApp.entity.User;
import Savina.ftiApp.repository.UserRepository;
import Savina.ftiApp.service.EmailService;
import Savina.ftiApp.service.PasswordResetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceUnitTest {

    @Mock
    private UserRepository userRepo;

    @Mock
    private EmailService emailService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private PasswordResetService passwordResetService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = User.builder()
                .userId(1)
                .email("user@fti.edu.al")
                .emri("Student")
                .mbiemri("FTI")
                .verificationCode("654321")
                .codeCreatedAt(LocalDateTime.now().minusMinutes(2))
                .build();
    }

    @Test
    @DisplayName("Unit: Dërgimi i kodit të rivendosjes gjeneron kod dhe thërret emailService")
    void testSendResetCodeSuccess() {
        when(userRepo.findByEmailIgnoreCase("user@fti.edu.al")).thenReturn(Optional.of(mockUser));

        ForgotPasswordRequest req = new ForgotPasswordRequest();
        req.setEmail("user@fti.edu.al");

        passwordResetService.sendResetCode(req);

        assertThat(mockUser.getVerificationCode()).isNotNull();
        assertThat(mockUser.getVerificationCode()).hasSize(6);
        verify(emailService, times(1)).sendPasswordResetCode(eq("user@fti.edu.al"), any(), any());
        verify(userRepo, times(1)).save(mockUser);
    }

    @Test
    @DisplayName("Unit: Verifikimi i kodit të rivendosjes kalon kur kodi është i saktë")
    void testVerifyResetCodeSuccess() {
        when(userRepo.findByEmailIgnoreCase("user@fti.edu.al")).thenReturn(Optional.of(mockUser));

        VerifyResetCodeRequest req = new VerifyResetCodeRequest();
        req.setEmail("user@fti.edu.al");
        req.setCode("654321");

        // Nuk duhet te hedhe exception
        passwordResetService.verifyResetCode(req);
    }

    @Test
    @DisplayName("Unit: Resetimi i fjalëkalimit kryhet me sukses me fjalëkalim kompleks")
    void testResetPasswordSuccess() {
        when(userRepo.findByEmailIgnoreCase("user@fti.edu.al")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.encode("Pass1234#")).thenReturn("hashed_new_password");

        ResetPasswordRequest req = new ResetPasswordRequest("user@fti.edu.al", "654321", "Pass1234#");

        passwordResetService.resetPassword(req);

        assertThat(mockUser.getPassword()).isEqualTo("hashed_new_password");
        assertThat(mockUser.getVerificationCode()).isNull();
        verify(userRepo, times(1)).save(mockUser);
    }

    @Test
    @DisplayName("Unit: Resetimi i fjalëkalimit dështon kur fjalëkalimi i ri nuk plotëson kriteret e kompleksitetit")
    void testResetPasswordWeakPassword() {
        when(userRepo.findByEmailIgnoreCase("user@fti.edu.al")).thenReturn(Optional.of(mockUser));

        ResetPasswordRequest req = new ResetPasswordRequest("user@fti.edu.al", "654321", "thjesht123");

        assertThatThrownBy(() -> passwordResetService.resetPassword(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Fjalekalimi duhet te kete te pakten 8 karaktere");

        verify(passwordEncoder, never()).encode(any());
    }
}
