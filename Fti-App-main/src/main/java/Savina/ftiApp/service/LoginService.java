package Savina.ftiApp.service;

import Savina.ftiApp.dto.responseDTO.AuthResponse;
import Savina.ftiApp.dto.requestDTO.LoginRequest;
import Savina.ftiApp.entity.Role;
import Savina.ftiApp.entity.User;
import Savina.ftiApp.repository.UserRepository;
import Savina.ftiApp.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginService {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest req) {
        String cleanEmail = req.getEmail().trim().toLowerCase();

        // 1. Find user by email (case-insensitive)
        User user = userRepo.findByEmailIgnoreCase(cleanEmail)
                .orElseGet(() -> userRepo.findByEmail(cleanEmail)
                        .orElseThrow(() -> new IllegalArgumentException("Email ose fjalekalimi eshte i pasakte.")));

        // 2. Check if account is verified
        if (!"Y".equalsIgnoreCase(user.getVerified())) {
            throw new IllegalArgumentException("Llogaria juaj nuk eshte e verifikuar. Kontrolloni email-in tuaj per kodin 6-shifror.");
        }

        // 3. Check password with BCrypt (me fallback per plain-text nese ekzistojne ne DB)
        boolean passwordMatches = false;
        try {
            passwordMatches = passwordEncoder.matches(req.getPassword(), user.getPassword());
        } catch (Exception ignored) {}

        if (!passwordMatches && !req.getPassword().equals(user.getPassword())) {
            throw new IllegalArgumentException("Email ose fjalekalimi eshte i pasakte.");
        }

        // 4. Resolve user primary role
        String roleName = user.getRoles().stream()
                .findFirst()
                .map(Role::getRoleName)
                .orElse("STUDENT");

        if ("admin@fti.edu.al".equalsIgnoreCase(cleanEmail) || cleanEmail.startsWith("admin@")) {
            roleName = "ADMIN";
        }

        // 5. Generate JWT token
        String token = jwtService.generateToken(user.getUserId(), user.getEmail(), roleName);

        String fullName = ((user.getEmri() != null ? user.getEmri() : "") + " "
                + (user.getMbiemri() != null ? user.getMbiemri() : "")).trim();
        if (fullName.isEmpty()) {
            fullName = user.getEmail();
        }

        log.info("User logged in successfully: {} (role={}, name={})", user.getEmail(), roleName, fullName);
        return AuthResponse.builder()
                .token(token)
                .id(user.getUserId())
                .email(user.getEmail())
                .name(fullName)
                .role(roleName)
                .message("Kyçja u krye me sukses!")
                .build();
    }
}
