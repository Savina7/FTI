package Savina.ftiApp.service;

import Savina.ftiApp.dto.responseDTO.AuthResponse;
import Savina.ftiApp.dto.requestDTO.LoginRequest;
import Savina.ftiApp.entity.LoginHistory;
import Savina.ftiApp.entity.Role;
import Savina.ftiApp.entity.User;
import Savina.ftiApp.repository.LoginHistoryRepository;
import Savina.ftiApp.repository.UserRepository;
import Savina.ftiApp.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginService {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final LoginHistoryRepository loginHistoryRepo;

    @Transactional
    public AuthResponse login(LoginRequest req) {
        String cleanEmail = req.getEmail().trim().toLowerCase();

        User user = userRepo.findByEmailIgnoreCase(cleanEmail)
                .orElseGet(() -> userRepo.findByEmail(cleanEmail)
                        .orElseThrow(() -> new IllegalArgumentException("Email ose fjalekalimi eshte i pasakte.")));

        if (!"Y".equalsIgnoreCase(user.getVerified())) {
            throw new IllegalArgumentException("Llogaria juaj nuk eshte e verifikuar. Kontrolloni email-in tuaj per kodin 6-shifror.");
        }

        boolean passwordMatches = false;
        try {
            passwordMatches = passwordEncoder.matches(req.getPassword(), user.getPassword());
        } catch (Exception ignored) {}

        if (!passwordMatches && !req.getPassword().equals(user.getPassword())) {
            throw new IllegalArgumentException("Email ose fjalekalimi eshte i pasakte.");
        }

        String roleName = user.getRoles().stream()
                .findFirst()
                .map(Role::getRoleName)
                .orElse("STUDENT");

        if ("admin@fti.edu.al".equalsIgnoreCase(cleanEmail) || cleanEmail.startsWith("admin@")) {
            roleName = "ADMIN";
        }

        String token = jwtService.generateToken(user.getUserId(), user.getEmail(), roleName);

        String fullName = ((user.getEmri() != null ? user.getEmri() : "") + " "
                + (user.getMbiemri() != null ? user.getMbiemri() : "")).trim();
        if (fullName.isEmpty()) {
            fullName = user.getEmail();
        }

        try {
            LoginHistory history = LoginHistory.builder()
                    .user(user)
                    .loginTime(LocalDateTime.now())
                    .status("SUCCESS")
                    .build();
            loginHistoryRepo.save(history);
        } catch (Exception e) {
            log.error("Gabim gjate ruajtjes se historikut te hyrjes per user {}: {}", user.getUserId(), e.getMessage());
        }

        log.info("User logged in successfully: {} (role={}, name={}, changePass={})", user.getEmail(), roleName, fullName, user.getChangePass());
        return AuthResponse.builder()
                .token(token)
                .id(user.getUserId())
                .email(user.getEmail())
                .name(fullName)
                .role(roleName)
                .changePass(user.getChangePass() != null ? user.getChangePass() : "NO")
                .message("Kyçja u krye me sukses!")
                .build();
    }

    @Transactional
    public AuthResponse forceChangePassword(Savina.ftiApp.dto.requestDTO.ForceChangePasswordRequest req) {
        String cleanEmail = req.getEmail().trim().toLowerCase();

        if (!req.getNewPassword().equals(req.getConfirmPassword())) {
            throw new IllegalArgumentException("Fjalekalimi i ri dhe konfirmimi i tij nuk perputhen.");
        }

        User user = userRepo.findByEmailIgnoreCase(cleanEmail)
                .orElseGet(() -> userRepo.findByEmail(cleanEmail)
                        .orElseThrow(() -> new IllegalArgumentException("Perdoruesi nuk u gjet.")));

        boolean currentMatches = false;
        try {
            currentMatches = passwordEncoder.matches(req.getCurrentPassword(), user.getPassword());
        } catch (Exception ignored) {}

        if (!currentMatches && !req.getCurrentPassword().equals(user.getPassword())) {
            throw new IllegalArgumentException("Fjalekalimi aktual (i perkohshem) eshte i pasakte.");
        }

        if (passwordEncoder.matches(req.getNewPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Fjalekalimi i ri nuk mund te jete i njejte me fjalekalimin e perkohshem.");
        }

        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        user.setChangePass("NO");
        user = userRepo.save(user);

        log.info("User {} successfully changed temporary password (changePass is now NO)", user.getEmail());

        String roleName = user.getRoles().stream()
                .findFirst()
                .map(Role::getRoleName)
                .orElse("STUDENT");

        if ("admin@fti.edu.al".equalsIgnoreCase(cleanEmail) || cleanEmail.startsWith("admin@")) {
            roleName = "ADMIN";
        }

        String token = jwtService.generateToken(user.getUserId(), user.getEmail(), roleName);
        String fullName = ((user.getEmri() != null ? user.getEmri() : "") + " "
                + (user.getMbiemri() != null ? user.getMbiemri() : "")).trim();
        if (fullName.isEmpty()) {
            fullName = user.getEmail();
        }

        return AuthResponse.builder()
                .token(token)
                .id(user.getUserId())
                .email(user.getEmail())
                .name(fullName)
                .role(roleName)
                .changePass("NO")
                .message("Fjalekalimi u ndryshua me sukses! Tani mund te vazhdoni ne sistem.")
                .build();
    }
}
