package Savina.ftiApp.service;

import Savina.ftiApp.entity.User;
import Savina.ftiApp.repository.ProfessorPreEnrollmentRepository;
import Savina.ftiApp.repository.StudentPreEnrollmentRepository;
import Savina.ftiApp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class VerificationService {

    private final UserRepository userRepo;
    private final EmailService   emailService;
    private final StudentPreEnrollmentRepository studentPreEnrollmentRepo;
    private final ProfessorPreEnrollmentRepository professorPreEnrollmentRepo;

    // ─── VERIFY CODE ────────────────────────────────────────────────────────────
    @Transactional
    public User verifyCode(Integer userId, String enteredCode) {

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("Perdoruesi nuk u gjet."));

        if ("Y".equals(user.getVerified())) {
            throw new IllegalArgumentException("Llogaria tashme eshte aktive. Hyni me login.");
        }

        if (user.getVerificationCode() == null || user.getCodeCreatedAt() == null) {
            throw new IllegalArgumentException("Nuk ka kod aktiv. Regjistrohuni serisht.");
        }

        if (LocalDateTime.now().isAfter(user.getCodeCreatedAt().plusMinutes(15))) {
            throw new IllegalArgumentException("Kodi ka skaduar (15 min). Klikoni 'Rindergo' per kod te ri.");
        }

        if (!user.getVerificationCode().equals(enteredCode.trim())) {
            throw new IllegalArgumentException("Kodi eshte i pasakte. Provoni serisht.");
        }

        user.setVerified("Y");
        user.setStatus("ACTIVE");
        user.setVerificationCode(null);
        user.setCodeCreatedAt(null);
        userRepo.save(user);

        // Update pre-enrollment status if exists
        if (user.getEmail() != null) {
            studentPreEnrollmentRepo.findByEmailIgnoreCase(user.getEmail().trim()).ifPresent(pe -> {
                pe.setStatus("VERIFIKUAR");
                studentPreEnrollmentRepo.save(pe);
            });
        }

        log.info("User {} verified successfully (userId={}).", user.getEmail(), userId);
        return user;
    }

    // ─── RESEND CODE ────────────────────────────────────────────────────────────
    @Transactional
    public void resendCode(Integer userId) {

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("Perdoruesi nuk u gjet."));

        if ("Y".equals(user.getVerified())) {
            throw new IllegalArgumentException("Llogaria tashme eshte aktive.");
        }

        String newCode = String.valueOf(100000 + new SecureRandom().nextInt(900000));

        user.setVerificationCode(newCode);
        user.setCodeCreatedAt(LocalDateTime.now());
        userRepo.save(user);

        emailService.sendVerificationCode(user.getEmail(),
                user.getEmri() + " " + user.getMbiemri(), newCode);

        log.info("Verification code resent to: {}", user.getEmail());
    }
}
