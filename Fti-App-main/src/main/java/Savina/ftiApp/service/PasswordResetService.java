package Savina.ftiApp.service;

import Savina.ftiApp.dto.requestDTO.ForgotPasswordRequest;
import Savina.ftiApp.dto.requestDTO.ResetPasswordRequest;
import Savina.ftiApp.dto.requestDTO.VerifyResetCodeRequest;
import Savina.ftiApp.entity.User;
import Savina.ftiApp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UserRepository userRepo;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    private static final String PASSWORD_REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z0-9]).{8,}$";

    @Transactional
    public void sendResetCode(ForgotPasswordRequest req) {
        String cleanEmail = req.getEmail().trim().toLowerCase();

        User user = userRepo.findByEmailIgnoreCase(cleanEmail)
                .orElseThrow(() -> new IllegalArgumentException("Nuk ekziston asnje llogari me kete adrese email-i."));

        String resetCode = String.valueOf(100000 + new SecureRandom().nextInt(900000));
        user.setVerificationCode(resetCode);
        user.setCodeCreatedAt(LocalDateTime.now());
        userRepo.save(user);

        String recipientName = "";
        if (user.getEmri() != null && !user.getEmri().isBlank()) {
            recipientName = user.getEmri() + (user.getMbiemri() != null ? " " + user.getMbiemri() : "");
        }

        emailService.sendPasswordResetCode(user.getEmail(), recipientName.trim(), resetCode);
        log.info("Kodi i rivendosjes se fjalekalimit u dergua me sukses per: {}", cleanEmail);
    }

    @Transactional(readOnly = true)
    public void verifyResetCode(VerifyResetCodeRequest req) {
        String cleanEmail = req.getEmail().trim().toLowerCase();

        User user = userRepo.findByEmailIgnoreCase(cleanEmail)
                .orElseThrow(() -> new IllegalArgumentException("Nuk ekziston asnje llogari me kete adrese email-i."));

        if (user.getVerificationCode() == null || user.getCodeCreatedAt() == null) {
            throw new IllegalArgumentException("Nuk ka asnje kod aktiv per rivendosje. Ju lutem kerkoni nje kod te ri.");
        }

        if (LocalDateTime.now().isAfter(user.getCodeCreatedAt().plusMinutes(15))) {
            throw new IllegalArgumentException("Kodi ka skaduar (vlen 15 minuta). Ju lutem kerkoni nje kod te ri.");
        }

        if (!user.getVerificationCode().equals(req.getCode().trim())) {
            throw new IllegalArgumentException("Kodi i verifikimit eshte i pasakte.");
        }

        log.info("Kodi i rivendosjes u verifikua me sukses per: {}", cleanEmail);
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest req) {
        String cleanEmail = req.getEmail().trim().toLowerCase();

        User user = userRepo.findByEmailIgnoreCase(cleanEmail)
                .orElseThrow(() -> new IllegalArgumentException("Nuk ekziston asnje llogari me kete adrese email-i."));

        if (user.getVerificationCode() == null || user.getCodeCreatedAt() == null) {
            throw new IllegalArgumentException("Nuk ka asnje kod aktiv per rivendosje. Ju lutem kerkoni nje kod te ri.");
        }

        if (LocalDateTime.now().isAfter(user.getCodeCreatedAt().plusMinutes(15))) {
            throw new IllegalArgumentException("Kodi ka skaduar (vlen 15 minuta). Ju lutem kerkoni nje kod te ri.");
        }

        if (!user.getVerificationCode().equals(req.getCode().trim())) {
            throw new IllegalArgumentException("Kodi i verifikimit eshte i pasakte.");
        }

        String newPassword = req.getNewPassword();
        if (newPassword == null || !newPassword.matches(PASSWORD_REGEX)) {
            throw new IllegalArgumentException("Fjalekalimi duhet te kete te pakten 8 karaktere, 1 shkronje te madhe, 1 te vogel, 1 numer dhe 1 karakter special.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setVerificationCode(null);
        user.setCodeCreatedAt(null);
        userRepo.save(user);

        log.info("Fjalekalimi u rivendos me sukses per perdoruesin: {}", cleanEmail);
    }
}
