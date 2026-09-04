package Savina.ftiApp.controller;

import Savina.ftiApp.dto.responseDTO.AuthResponse;
import Savina.ftiApp.dto.requestDTO.LoginRequest;
import Savina.ftiApp.dto.requestDTO.ForgotPasswordRequest;
import Savina.ftiApp.dto.requestDTO.ResetPasswordRequest;
import Savina.ftiApp.service.LoginService;
import Savina.ftiApp.service.PasswordResetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import Savina.ftiApp.dto.requestDTO.VerifyResetCodeRequest;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class LoginController {

    private final LoginService loginService;
    private final PasswordResetService passwordResetService;

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest req) {
        return loginService.login(req);
    }

    @PostMapping("/forgot-password")
    public AuthResponse forgotPassword(@Valid @RequestBody ForgotPasswordRequest req) {
        passwordResetService.sendResetCode(req);
        return new AuthResponse("Kodi i verifikimit u dergua me sukses ne adresen tuaj te email-it.");
    }

    @PostMapping("/verify-reset-code")
    public AuthResponse verifyResetCode(@Valid @RequestBody VerifyResetCodeRequest req) {
        passwordResetService.verifyResetCode(req);
        return new AuthResponse("Kodi u verifikua me sukses!");
    }

    @PostMapping("/reset-password")
    public AuthResponse resetPassword(@Valid @RequestBody ResetPasswordRequest req) {
        passwordResetService.resetPassword(req);
        return new AuthResponse("Fjalekalimi juaj u ndryshua me sukses! Tani mund te hyni.");
    }
}
