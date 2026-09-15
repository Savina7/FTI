package Savina.ftiApp.unit;

import Savina.ftiApp.dto.requestDTO.LoginRequest;
import Savina.ftiApp.dto.responseDTO.AuthResponse;
import Savina.ftiApp.entity.LoginHistory;
import Savina.ftiApp.entity.Role;
import Savina.ftiApp.entity.User;
import Savina.ftiApp.repository.LoginHistoryRepository;
import Savina.ftiApp.repository.UserRepository;
import Savina.ftiApp.security.JwtService;
import Savina.ftiApp.service.LoginService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginServiceUnitTest {

    @Mock
    private UserRepository userRepo;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private LoginHistoryRepository loginHistoryRepo;

    @InjectMocks
    private LoginService loginService;

    private User mockUser;
    private LoginRequest validRequest;

    @BeforeEach
    void setUp() {
        Role studentRole = new Role();
        studentRole.setRoleId(1);
        studentRole.setRoleName("STUDENT");

        mockUser = User.builder()
                .userId(10)
                .email("student@fti.edu.al")
                .password("encoded_pass_123")
                .emri("Agim")
                .mbiemri("Hoxha")
                .verified("Y")
                .roles(Set.of(studentRole))
                .build();

        validRequest = new LoginRequest();
        validRequest.setEmail("student@fti.edu.al");
        validRequest.setPassword("plain_pass_123");
    }

    @Test
    @DisplayName("Unit: Login i suksesshëm me kredenciale të sakta gjeneron JWT dhe ruan LoginHistory")
    void testLoginSuccess() {
        when(userRepo.findByEmailIgnoreCase("student@fti.edu.al")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("plain_pass_123", "encoded_pass_123")).thenReturn(true);
        when(jwtService.generateToken(eq(10), eq("student@fti.edu.al"), eq("STUDENT"))).thenReturn("mock_jwt_token");

        AuthResponse response = loginService.login(validRequest);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("mock_jwt_token");
        assertThat(response.getEmail()).isEqualTo("student@fti.edu.al");
        assertThat(response.getName()).isEqualTo("Agim Hoxha");
        assertThat(response.getRole()).isEqualTo("STUDENT");

        verify(loginHistoryRepo, times(1)).save(any(LoginHistory.class));
    }

    @Test
    @DisplayName("Unit: Login dështon kur përdoruesi nuk ekziston në bazë të dhënash")
    void testLoginUserNotFound() {
        when(userRepo.findByEmailIgnoreCase("unknown@fti.edu.al")).thenReturn(Optional.empty());
        when(userRepo.findByEmail("unknown@fti.edu.al")).thenReturn(Optional.empty());

        LoginRequest req = new LoginRequest();
        req.setEmail("unknown@fti.edu.al");
        req.setPassword("some_pass");

        assertThatThrownBy(() -> loginService.login(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email ose fjalekalimi eshte i pasakte.");

        verify(loginHistoryRepo, never()).save(any());
    }

    @Test
    @DisplayName("Unit: Login dështon kur llogaria nuk është verifikuar me kod (verified='N')")
    void testLoginUnverifiedAccount() {
        mockUser.setVerified("N");
        when(userRepo.findByEmailIgnoreCase("student@fti.edu.al")).thenReturn(Optional.of(mockUser));

        assertThatThrownBy(() -> loginService.login(validRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Llogaria juaj nuk eshte e verifikuar");

        verify(passwordEncoder, never()).matches(any(), any());
        verify(loginHistoryRepo, never()).save(any());
    }

    @Test
    @DisplayName("Unit: Login dështon kur fjalëkalimi është i pasaktë")
    void testLoginWrongPassword() {
        when(userRepo.findByEmailIgnoreCase("student@fti.edu.al")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("plain_pass_123", "encoded_pass_123")).thenReturn(false);

        assertThatThrownBy(() -> loginService.login(validRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email ose fjalekalimi eshte i pasakte.");

        verify(jwtService, never()).generateToken(any(), any(), any());
        verify(loginHistoryRepo, never()).save(any());
    }
}
